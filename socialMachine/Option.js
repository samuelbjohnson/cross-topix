

dojo.declare("net.samuelbjohnson.jsdev.crosstopix.Option", null, {
	constructor: function(/*node*/ parentNode) {
		this.container = dojo.create("div", {class: "option"}, parentNode);
	},
	
	setup: function(/*string*/title, /*string*/url) {
		this.link = dojo.create("a", {href: url}, this.container);
		this.link.innerHTML = title;
	}
});