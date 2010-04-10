<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head profile="http://gmpg.org/xfn/11">
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="google-site-verification"
	content="8RxwY8OzQyC4XLdbe361fAXgdQVaiUtPAinH1OO67Wo" />
<title>PTP Test</title>
<style type="text/css">
body {
	font-family: Arial;
	font-size: 10px
}

textarea {
	width: 800px;
	height: 500px;
	font-family: Arial;
	font-size: 10px
}
</style>
</head>
<body>
<form name="form" action="index.php"
	method="post"><input name="request_data" type="text" /> <input
	name="dest_host" type="text" /> <input name="dest_port" type="text" />
<input name="submit" type="submit" /></form>
<?php
$dest_host = $_POST['dest_host'];
$dest_port = $_POST['dest_port'];
if($dest_port == 443) {
	if(in_array('ssl', stream_get_transports())) {
		$dest_host="ssl://".$dest_host;
	} else {
		$dest_port=80;
	}
}
$request = base64_decode($_POST['request_data']);

if(empty($dest_host)) $dest_host="twitter.com";
if(empty($dest_port)) $dest_port=80;
if(empty($request)) $request=base64_decode("R0VUIC8gSFRUUC8xLjENCkhvc3Q6IHR3aXR0ZXIuY29tDQpVc2VyLUFnZW50OiBNb3ppbGxhLzUuMCAoV2luZG93czsgVTsgV2luZG93cyBOVCA1LjE7IGVuLVVTOyBydjoxLjkuMi4zKSBHZWNrby8yMDEwMDQwMSBGaXJlZm94LzMuNi4zICguTkVUIENMUiAzLjUuMzA3MjkpDQpBY2NlcHQ6IHRleHQvaHRtbCxhcHBsaWNhdGlvbi94aHRtbCt4bWwsYXBwbGljYXRpb24veG1sO3E9MC45LCovKjtxPTAuOA0KQWNjZXB0LUxhbmd1YWdlOiBlbi11cyxlbjtxPTAuNQ0KQWNjZXB0LUVuY29kaW5nOiBnemlwLGRlZmxhdGUNCkFjY2VwdC1DaGFyc2V0OiBJU08tODg1OS0xLHV0Zi04O3E9MC43LCo7cT0wLjcNClJlZmVyZXI6IGh0dHA6Ly90d2l0dGVyLmNvbS8NCkNvb2tpZTogX191dG1hPTQzODM4MzY4LjY3MTc1ODk5NS4xMjY3NTIwMjM0LjEyNzA3MzU5NTYuMTI3MDczODc0My4xNzc7IF9fdXRtej00MzgzODM2OC4xMjcwMDU0MTgwLjEzOC42LnV0bWNzcj1mb2xsb3dyZXF8dXRtY2NuPXR3aXR0ZXIyMDA4MDMzMTE2MjYzMXx1dG1jbWQ9ZW1haWx8dXRtY2N0PWZvbGxvd3JlcXM7IF9fdXRtdj00MzgzODM2OC5sYW5nJTNBJTIwZW47IGdlb193ZWJjbGllbnQ9MTsgZ2VvX2ZmX2Jhbm5lcl9zZWVuPTE7IF9fcWNhPVAwLTM4NTQ5ODc4Mi0xMjY4NTc5NDk2MTk5OyBndWVzdF9pZD0xMjcwMDAyNzE4NDMzOyBfdHdpdHRlcl9zZXNzPUJBaDdFam9QWTNKbFlYUmxaRjloZEd3ckNLczlTZDBuQVNJcWMyaHZkMTlrYVhOamIzWmxjbUZpYVd4cCUyNTBBZEhsZlptOXlYM2h0ZUhOMWNHVnljM1JoY2pBNkUzQmhjM04zYjNKa1gzUnZhMlZ1TURvTVkzTnlabDlwJTI1MEFaQ0lsTXpFMk5HSmpabUl3TVRnelpEUXlNMll4TldJNU9EZGhabUUzT1RGbE5HSTZEV0ZrYldsdVgybGslMjUwQU1Eb1ZhVzVmYm1WM1gzVnpaWEpmWm14dmR6QTZFWFJ5WVc1elgzQnliMjF3ZERBNkZYZGxZV3RmWlcxdyUyNTBBYkc5NVpXVmZjSGN3T2dsMWMyVnlNQ0lLWm14aGMyaEpRem9uUVdOMGFXOXVRMjl1ZEhKdmJHeGxjam82JTI1MEFSbXhoYzJnNk9rWnNZWE5vU0dGemFIc0FCam9LUUhWelpXUjdBRG9IYVdRaUpUUmpZbVprWldKalptSXklMjUwQVpqWmxPRFV3TURSa01UUXhPR0poTkdOaFpHWXhPaGRqYjI1MGNtbGlkWFJwYm1kZmRHOWZhV1F3T2c1eSUyNTBBWlhSMWNtNWZkRzh3LS03M2ExMzQ4NjBmMGJhODQwNWYxZWVkZmM0YjMxMDZjMGMxNTEyZDYwOyBfX3V0bWM9NDM4MzgzNjg7IG9yaWdpbmFsX3JlZmVyZXI9NGJmeiUyQiUyQm1lYkVrUmtNV0ZDWG0lMkZDVU9zdkRvVmVGVGw7IF9fdXRtYj00MzgzODM2OC43LjEwLjEyNzA3Mzg3NDMNCkNhY2hlLUNvbnRyb2w6IG1heC1hZ2U9MA0KQ29ubmVjdGlvbjogY2xvc2UNCg0K");

echo "host: $dest_host <br/>";
echo "port: $dest_port <br/>";
echo "<pre>$request</pre>";
?>
<textarea>
<?php
$fp = fsockopen($dest_host, $dest_port, $errno, $errstr, 30);
if (!$fp) {
	echo "$errstr ($errno)<br />\n";
} else {
	fwrite($fp, $request);
	while (!feof($fp)) {
		$buffer = fread($fp, 1024);
		echo htmlentities($buffer);
	}
	fclose($fp);
}
?>
</textarea>
</body>
</html>
