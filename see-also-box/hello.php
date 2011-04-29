<?php
$wiki_page = 'http://imslp.org/wiki/Stabat_Mater,_Op.58_(Dvo%C5%99%C3%A1k,_Anton%C3%ADn)'; //$_GET['page'];
$ENDPOINT = 'http://leo.tw.rpi.edu:81/endpoint.php'; 

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

static function getSuggestions($wiki_page, $ENDPOINT) {
 $query = <<<______________________________
PREFIX dcterms: <http://purl.org/dc/terms/>
prefix xt:  	<http://purl.org/twc/vocab/cross-topix#>
SELECT ?other ?title ?sim ?user ?accepted
WHERE {
  GRAPH <http://leo.tw.rpi.edu/source/orange/dataset/crowd-verifications/version/2011-Apr-25> {
	?vote a xt:ComparisonReview;
  	xt:comparison ?comparison;
  	xt:user_name  ?user;
  	xt:accepted   ?accepted;
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
} ORDER BY ?sim ?accepted ?user ?other ?title LIMIT 10
______________________________;

   $query = crossTopix::bind_variable($query,'?:page',$wiki_page);

   $result = crossTopix::request_query($query, $ENDPOINT);
  
   if( isset($result['results']['bindings']) ) {
      foreach($result['results']['bindings'] as $binding){
         echo '<p> <a href="' . $binding['other']['value'] . '">' . $binding['title']['value'] . '</a> </p>';
         
         $val = $binding['sim']['value'];
         echo  '<p> Machine value = ' . $val . '</p>';
         
         echo '<center> <p>';
         if ($val < 0.3) {
            echo '<img border="0" src="/full-star.png" width="30" height="30" />';
         } else {
            echo '<img border="0" src="/empty-star.png" width="30" height="30" />';
         } if ($val < 0.65) {
            echo '<img border="0" src="/full-star.png" width="30" height="30" />';
         } else {
            echo '<img border="0" src="/empty-star.png" width="30" height="30" />';
         } if ($val < 0.8) {
            echo '<img border="0" src="/full-star.png" width="30" height="30" />';
         } else {
            echo '<img border="0" src="/empty-star.png" width="30" height="30" />';
         }
         echo '</p> </center>';
      }
   }
   return;
} // end of getSuggestions

} // end of class
echo '<html>';
echo '<body>';

crossTopix::getSuggestions($wiki_page, $ENDPOINT);

echo '</body>';
echo '</html>';
?> 
