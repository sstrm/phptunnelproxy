<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">
	<head profile="http://gmpg.org/xfn/11">
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<link rel="stylesheet" href="css/box.css" type="text/css" media="screen" />
		<script type="text/javascript" src="js/jquery-1.4.3.min.js"></script>
		<script type="text/javascript" src="js/ptppac.js"></script>
		<title>PTP GFWLIST2PAC</title>
	</head>
	<body>
	<div class="container">
		<div class="top">
			<h1><a href="">PTP GFWLIST2PAC</a></h1>
		</div>
		<div class="center">
			<div class="content">
						<form action="pac.php" method="get" id="config">
							<fieldset id="proxy-select">
								<legend><span>请选择你使用的代理工具</span></legend>
								<p><label><input type="radio" value="free-gate" name="proxy_name" /> 自由门</label></p>
								<p><label><input type="radio" value="tor" name="proxy_name" /> Tor</label></p>
								<p><label><input type="radio" value="ssh-d" name="proxy_name" /> ssh -D / MyEnTunnel</label></p>
								<p><label><input type="radio" value="gappproxy" name="proxy_name" /> GAppProxy</label></p>
								<p><label><input type="radio" value="jap" name="proxy_name" /> JAP</label></p>
								<p><label><input type="radio" value="your-freedom" name="proxy_name" /> Your Freedom</label></p>
								<p><label><input type="radio" value="puff" name="proxy_name" /> Puff</label></p>
								<p><label><input type="radio" value="privoxy" name="proxy_name" /> Privoxy + SOCKS</label></p>
								<p><label><input type="radio" value="wu-jie" name="proxy_name" /> 无界</label></p>
								<p>
									<label><input type="radio" value="custom" name="proxy_name" checked="checked"/> 其它</label>
									<span id="proxy-input">
										<select name="proxy_type">
											<option value="http">HTTP</option>
											<option value="socks">SOCKS</option>
										</select> 
										<input size="15" value="127.0.0.1"  name="proxy_host" />
										:
										<input size="5" value="8080" name="proxy_port" />
									</span>
								</p>
							</fieldset>
							<button name="get-pac" value="pac" onclick="insert_pac_url()">获取PAC</button>
						</form>
						<div id="pac-url"></div>
					</div>
				</div>
			<div class="bottom">
				<a href="http://code.google.com/p/phptunnelproxy/">PTP</a>
				<span>|</span>
				<a href="http://code.google.com/p/autoproxy-gfwlist/">GFWLIST</a>
				<span>|</span>
				<a href="http://autoproxy.org/">Autoproxy</a>
				<span>|</span>
				<a href="http://autoproxy2pac.appspot.com/">Autoproxy2pac</a>
			</div>
		</div>
	<div class="footer"></div>
</body>
</html>
