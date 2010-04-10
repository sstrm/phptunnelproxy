<?php
    $url = 'https://www.google.com/accounts/ServiceLogin?service=mail&amp;passive=true&amp;rm=false&amp;continue=https%3A%2F%2Fmail.google.com%2Fmail%2F%3Fui%3Dhtml%26zy%3Dl&amp;bsv=1k96igf4806cy&amp;ss=1&amp;ltmpl=default&amp;ltmplcache=2';
    $user_agent = "Mozilla/4.0 (compatible; MSIE 5.01; Windows NT 5.0)";

    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL,$url);
    curl_setopt($ch, CURLOPT_SSL_VERIFYHOST,  2);
    curl_setopt($ch, CURLOPT_USERAGENT, $user_agent);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER,1);
    curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, FALSE);  // this line makes it work under https

    $result=curl_exec ($ch);
    echo("Results: <br>".$result);
        echo(curl_error($ch));
    curl_close ($ch);
?> 