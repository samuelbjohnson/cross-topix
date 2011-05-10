<?php
$wiki_page = $_GET['page'];
$ENDPOINT = 'http://leo.tw.rpi.edu:81/endpoint.php'; 

// determine if on imslp or cpdl
preg_match( "#^(http://)?([^/:]+)#i", $wiki_page, $domain);
if($domain[0] == "http://imslp.org")
	$wiki = "0";
else
	$wiki = "1";

class crossTopix {

   /* Utility functions */
   static function bind_variable($template, $variable, $binding) {
      return str_replace($variable,'<'.$binding.'>',$template);
   }

   static function prepare_query($query, $endpoint) {
      $params           = array();
      $params["query"]  = $query;
      $params["output"] = 'json';
      $query= $endpoint . '?' . http_build_query($params,'','&') ;
      return $query;
   }

   static function request_query($query, $endpoint) {
      return json_decode(file_get_contents(crossTopix::prepare_query($query, $endpoint)), true);
   }

static function getSuggestions($wiki_page, $ENDPOINT, $wiki) {
$query1 = <<<______________________________
PREFIX dcterms: <http://purl.org/dc/terms/>
prefix xt:  	<http://purl.org/twc/vocab/cross-topix#>
SELECT DISTINCT ?other ?title ?sim ?user ?date
WHERE {
  GRAPH <http://leo.tw.rpi.edu/source/orange/dataset/crowd-verifications/version/2011-Apr-25> {
	?vote a xt:ComparisonReview;
  	xt:comparison ?comparison;
  	xt:user_name  ?user;
  	xt:accepted   true;
        dcterms:created ?date;
	.
  }
  GRAPH <http://leo.tw.rpi.edu/source/orange-joiner/dataset/title-similarities/version/2011-Apr-20> {
	?comparison xt:comparable_1 ?:page;
            	xt:comparable_2 ?other;
            	xt:similarity   ?sim .
  }
  GRAPH <http://leo.tw.rpi.edu/source/orange-amanda/dataset/ground-truth/version/2011-Apr-19> {
	?other dcterms:title ?title .
  }
} ORDER BY ?sim ?date ?user ?other ?title LIMIT 10
______________________________;

$query2 = <<<______________________________
PREFIX dcterms: <http://purl.org/dc/terms/>
prefix xt:  	<http://purl.org/twc/vocab/cross-topix#>
SELECT DISTINCT ?other ?title ?sim ?user
WHERE {
  GRAPH <http://leo.tw.rpi.edu/source/orange/dataset/crowd-verifications/version/2011-Apr-25> {
	?vote a xt:ComparisonReview;
  	xt:comparison ?comparison;
  	xt:user_name  ?user;
  	xt:accepted   true;
	.
  }
  GRAPH <http://leo.tw.rpi.edu/source/orange-joiner/dataset/title-similarities/version/2011-Apr-20> {
	?comparison xt:comparable_1 ?other;
            	xt:comparable_2 ?:page;
            	xt:similarity   ?sim .
  }
  GRAPH <http://leo.tw.rpi.edu/source/orange-amanda/dataset/ground-truth/version/2011-Apr-19> {
	?other dcterms:title ?title .
  }
} ORDER BY ?sim ?user ?other ?title LIMIT 10
______________________________;

   if ($wiki == "0")
      $query = crossTopix::bind_variable($query1,'?:page',$wiki_page);
  else
      $query = crossTopix::bind_variable($query2,'?:page',$wiki_page);

   $result = crossTopix::request_query($query, $ENDPOINT);
    
   if( isset($result['results']['bindings']) ) {
      foreach($result['results']['bindings'] as $binding){
         $val = $binding['sim']['value'];
	 $date = $binding['date']['value'];
	 if ($prev == $binding['title']['value'])
	 	continue;
	 $prev = $binding['title']['value'];

	if ($wiki == "0") // if imslp
		echo '<td rowspan="2" align="center"> <img border="0" src="link-imslp-to-cpdl.png" height="70" /></td>';
	else
		echo '<td rowspan="2" align="center"> <img border="0" src="link-cpdl-to-imslp.png" height="70" /></td>';

	echo '<td rowspan="2"> <a href="' . $binding['other']['value'] . '">' . $binding['title']['value'] . '</a> </td>';
	echo '<td align="center">';
        if ($val < 0.8)
            echo '<img border="0" src="/full-star.png" width="30" height="30" alt="'.$date.'"/>';
        else
            echo '<img border="0" src="/empty-star.png" width="30" height="30" alt="'.$date.'"/>';
        if ($val < 0.65)
            echo '<img border="0" src="/full-star.png" width="30" height="30" alt="'.$date.'"/>';
        else
            echo '<img border="0" src="/empty-star.png" width="30" height="30" alt="'.$date.'"/>';
        if ($val < 0.3)
            echo '<img border="0" src="/full-star.png" width="30" height="30" alt="'.$date.'"/>';
        else
            echo '<img border="0" src="/empty-star.png" width="30" height="30" alt="'.$date.'"/>';
        echo '</td>';
	echo '</tr>';
	echo '<tr>';
	echo '<td align="center">Value: ' . round($val,2);
	echo '</td>';
	echo '</tr>';
      }
   }
   return;
} // end of getSuggestions

} // end of class


echo '<html>';
echo '<head>';

echo '</head>';
echo '<body>';
echo '<center>';
echo '<table border="3" rules="NONE">';         
echo '<tr>';
echo '<td colspan="3">';
echo 'Currently looking at: <a href="'.$wiki_page.'">'.$wiki_page.'</a>';
echo '</td></tr><tr>';
echo '<td colspan="3" height="40px"></td>';
echo '</tr><tr>';
echo '<td colspan="3"><font size="5"><b>';
echo 'We think you might also be interested in these other pages on ';
if ($wiki == "0")
	echo '<a href="http://www3.cpdl.org/wiki">cpdl.org</a>:';
else
	echo '<a href="http://imslp.org/wiki">imslp.org</a> :';
echo '</b></font></td></tr>';
echo '<tr><td colspan="3" height="20px"></td></tr>';

crossTopix::getSuggestions($wiki_page, $ENDPOINT, $wiki);
echo '<tr><td colspan="3" height="40px"></td></tr>';
echo '<tr><td colspan="3" align="center">';
echo 'Think you can make better suggestions? Head over to the <a href ="http://leo.tw.rpi.edu:81/cross-topix/socialMachine">Social Machine</a> to help out!';
//echo '<img border="0" src="/splash-screen.png" height="70" />';
//echo '<p style="color:green">';
//echo 'Brought to you by Cross-Topix';
//echo '</p>';
echo '</td></tr>';
echo '</table>';
echo '</center>';
echo '</body>';
echo '</html>';
?> 
