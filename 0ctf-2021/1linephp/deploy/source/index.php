<?php
($_=@$_GET['yxxx'].'.php') && @substr(file($_)[0],0,6) === '@<?php' ? include($_) : highlight_file(__FILE__) && include('phpinfo.html');
