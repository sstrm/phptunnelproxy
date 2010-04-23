<?php
function reg_encode($str) {
	$tmp_str=$str;
	$tmp_str=str_replace('/', "\\/", $tmp_str);
	$tmp_str=str_replace('.', "\\.", $tmp_str);
	$tmp_str=str_replace(':', "\\:", $tmp_str);
	$tmp_str=str_replace('%', "\\%", $tmp_str);
	$tmp_str=str_replace('*', "\\.*", $tmp_str);
	$tmp_str=str_replace('-', "\\-", $tmp_str);
	$tmp_str=str_replace('&', "\\&", $tmp_str);
	$tmp_str=str_replace('?', "\\?", $tmp_str);
	
	return $tmp_str;
}

header('Content-Type: text/plain');

if(!empty($_GET['type']))
	if($_GET['type']=='http')
		$type='PROXY';
	else if ($_GET['type']=='socks')
		$type='SOCKS5';
	else
		$type=$_GET['type'];

$host=!empty($_GET['host'])?$_GET['host']:'127.0.0.1';
$port=!empty($_GET['port'])?$_GET['port']:'8080';
$gfwlist_url='http://autoproxy-gfwlist.googlecode.com/svn/trunk/gfwlist.txt';

$ch=curl_init();  
curl_setopt($ch, CURLOPT_URL, $gfwlist_url);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
$content=curl_exec($ch);
curl_close($ch);
$gfwlist=explode("\n", base64_decode($content)); 

?>
function FindProxyForURL(url, host) {
	var PROXY = "<?php echo $type;?> <?php echo $host;?>:<?php echo $port;?>";
	var DEFAULT = "DIRECT";
	
	<?php
		foreach($gfwlist as $index=>$rule){
			if(empty($rule))
				continue;
			else if(substr($rule,0,1)=='!' || substr($rule,0,1)=='[')
				continue;
			$return_proxy='PROXY';
			if(substr($rule,0,2)=='@@')
			{
				$rule=substr($rule,2);
				$return_proxy="DEFAULT";
			}
			else if(substr($rule,0,2)=='||')
			{
				$rule_reg = "/^[\\w\\-]+:\\/+(?!\\/)(?:[^\\/]+\\.)?".reg_encode(substr($rule,2))."/";
			}
			else if(substr($rule,0,1)=='|')
			{
				$rule_reg = "/^".reg_encode(substr($rule,1))."/";
			}
			else if(substr($rule,0,1)=='/'&&substr($rule,-1)=='/')
			{
				$rule_reg = $rule;
			}
			else
			{
				$rule_reg="/".reg_encode($rule)."/";
			}
			echo 'if('.$rule_reg.'i.test(url)) return '.$return_proxy.';'."\n\t";
		}
	?>
	
	return DEFAULT;
}
