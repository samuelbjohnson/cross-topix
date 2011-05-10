dojo.require("dojo.io.script");
dojo.require("dijit.form.Button");
dojo.require("dojo.date.locale");

dojo.declare("net.samuelbjohnson.jsdev.crosstopix.SocialMachine", null, {
	constructor: function(/*node*/containerNode) {
		this.container = containerNode;
		
		this.buildHtml();
		this.offset = -1;
		
		this.obtainUsername();
		
		this.runQuery();
	},
	
	buildHtml: function() {
		this.labelDiv = dojo.create("div", {class: "label"}, this.container);
		this.labelDiv.innerHTML = "Are these two pages about the same thing?";
		
		this.optionDiv = dojo.create("div", {}, this.container);
		
		this.choiceDiv = dojo.create("div", {class: "choice"}, this.container);
		this.yesButton = new dijit.form.Button({
			label: "Yes",
			onClick: dojo.hitch(this, this.processYes)
		}, dojo.create("div", {}, this.choiceDiv));
		this.noButton = new dijit.form.Button({
			label: "No",
			onClick: dojo.hitch(this, this.processNo)
		}, dojo.create("div", {}, this.choiceDiv));
	},
	
	obtainUsername: function() {
		this.userName = "basicUser";
	},
	
	constructGetMatchQuery: function() {
		this.offset += 1;
		return {
			output: "json",
			key: "",
			query: "prefix dcterms: <http://purl.org/dc/terms/> \
prefix cpdl:	<http://www3.cpdl.org/wiki/index.php/> \
prefix imslp:   <http://imslp.org/wiki/> \
prefix xt:  	<http://purl.org/twc/vocab/cross-topix#> \
 \
SELECT ?comparison ?page_1 ?page_2 ?sim ?title_1 ?title_2 \
 \
WHERE { \
  GRAPH <http://leo.tw.rpi.edu/source/orange-joiner/dataset/title-similarities/version/2011-Apr-20> { \
 	?comparison xt:comparable_1 ?page_1; \
             	xt:comparable_2 ?page_2; \
             	xt:similarity   ?sim . \
  } \
  GRAPH <http://leo.tw.rpi.edu/source/orange-amanda/dataset/ground-truth/version/2011-Apr-19> { \
 	?page_1 dcterms:title ?title_1 . \
  } \
  GRAPH <http://leo.tw.rpi.edu/source/orange-amanda/dataset/ground-truth/version/2011-Apr-19> { \
 	?page_2 dcterms:title ?title_2 . \
  } \
} ORDER BY ASC(?sim) LIMIT 1 OFFSET " + this.offset
		};
	},
	
	/*
		response parameter should be a string representation of a boolean
	*/
	constructPostResponseQuery: function(/*string*/response) {
		var now, dateString, timestampString, comparison, outputString;
		now = new Date();
		dateString = dojo.date.locale.format(now, {
			selector: "date",
			datePattern: "yyyy'_'MM'_'dd'T'HH'_'mm'_'ss"
		});
		timestampString = dojo.date.locale.format(now, {
			selector: "date",
			datePattern: "yyyy'-'MM'-'dd'T'HH':'mm':'ssZ"
		});
		
		comparison = this.data["results"]["bindings"][0]["comparison"]["value"];
		
		outputString =  
			"output=json&key=&" + 
			"query=" + "prefix xsd: <http://www.w3.org/2001/XMLSchema#> \
prefix xt:  <http://purl.org/twc/vocab/cross-topix#> \
prefix dcterms: <http://purl.org/dc/terms/> \
prefix vote:  <http://leo.tw.rpi.edu/source/orange/dataset/crowd-verifications/version/2011-Apr-25/typed/vote/> \
 \
INSERT INTO <http://leo.tw.rpi.edu/source/orange/dataset/crowd-verifications/version/2011-Apr-25> { \
 \
  vote:" + this.userName + "_" + dateString + " \
  a xt:ComparisonReview; \
 	xt:comparison <"
    	+ comparison + ">; \
 	xt:user_name  \"" + this.userName + "\"; \
 	xt:accepted   " + response + "; \
 	dcterms:created \""  + timestampString + "\"^^xsd:dateTime ; \
  . \
  }";
  
		return outputString;
	},
	
	processProposedMatch: function(/*json object*/data) {
		console.log("received data: ", data);
		this.data = data;
		
		dojo.empty(this.optionDiv);
		
		this.optionOne = new net.samuelbjohnson.jsdev.crosstopix.Option(this.optionDiv);
		
		this.optionTwo = new net.samuelbjohnson.jsdev.crosstopix.Option(this.optionDiv);
		
		this.optionOne.setup(
			data["results"]["bindings"][0]["title_1"]["value"],
			data["results"]["bindings"][0]["page_1"]["value"]
		);
		
		this.optionTwo.setup(
			data["results"]["bindings"][0]["title_2"]["value"],
			data["results"]["bindings"][0]["page_2"]["value"]
		);
	},
	
	processFinishedInsert: function(/*json object*/ data) {
		console.log("insert returned");
		this.runQuery();
	},
	
	processYes: function() {
		var queryObject;
		console.log("user says yes");
		queryObject = this.constructPostResponseQuery("true");
		this.postResponse(queryObject);
	},
	
	processNo: function() {
		var queryObject;
		console.log("user says no");
		queryObject = this.constructPostResponseQuery("false");
		this.postResponse(queryObject);
	},
	
	postResponse: function(queryString) {
		var xhrArgs;
		
		xhrArgs = {
			url: "http://leo.tw.rpi.edu:81/endpoint.php?",
			postData: queryString,
			handleAs: "json",
			load: dojo.hitch(this, this.processFinishedInsert),
			error: dojo.hitch(this, this.processAjaxError)
		};
		
		dojo.xhrPost(xhrArgs);
	},
	
	processAjaxError: function() {
		alert("ajax error");
	},
	
	runQuery: function() {
		var query, queryString, xhrArgs, result;
		
		query = this.constructGetMatchQuery();
		
		queryString = dojo.objectToQuery(query);
		
		xhrArgs = {
			url: "http://leo.tw.rpi.edu:81/endpoint.php?",
			callbackParamName: "jsonp",
			content: query,
			load: dojo.hitch(this, this.processProposedMatch),
			error: dojo.hitch(this, this.processAjaxError)
		};
		
		dojo.io.script.get(xhrArgs);
	}
});