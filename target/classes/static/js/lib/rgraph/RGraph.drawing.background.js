
RGraph=window.RGraph||{isRGraph:true};RGraph.Drawing=RGraph.Drawing||{};RGraph.Drawing.Background=function(conf)
{if(typeof conf==='object'&&typeof conf.id==='string'){var id=conf.id,canvas=document.getElementById(id),parseConfObjectForOptions=true;}else{var id=conf,canvas=document.getElementById(id);}
this.id=id;this.canvas=document.getElementById(this.id);this.context=this.canvas.getContext('2d');this.canvas.__object__=this;this.original_colors=[];this.firstDraw=true;this.propertyNameAliases={};this.type='drawing.background';this.isRGraph=true;this.uid=RGraph.CreateUID();this.canvas.uid=this.canvas.uid?this.canvas.uid:RGraph.createUID();this.properties={'chart.background.bars.count':null,'chart.background.bars.color1':'rgba(0,0,0,0)','chart.background.bars.color2':'rgba(0,0,0,0)','chart.background.grid':true,'chart.background.grid.color':'#ddd','chart.background.grid.linewidth':1,'chart.background.grid.vlines':true,'chart.background.grid.hlines':true,'chart.background.grid.border':true,'chart.background.grid.autofit':true,'chart.background.grid.hlines.count':5,'chart.background.grid.vlines.count':20,'chart.background.grid.dashed':false,'chart.background.grid.dotted':false,'chart.background.image':null,'chart.background.image.stretch':true,'chart.background.image.x':null,'chart.background.image.y':null,'chart.background.image.w':null,'chart.background.image.h':null,'chart.background.image.align':null,'chart.background.color':null,'chart.margin.left':25,'chart.margin.right':25,'chart.margin.top':25,'chart.margin.bottom':30,'chart.text.color':'black','chart.text.size':12,'chart.text.font':'Arial, Verdana, sans-serif','chart.text.bold':false,'chart.text.italic':false,'chart.text.accessible':true,'chart.text.accessible.overflow':'visible','chart.text.accessible.pointerevents':false,'chart.events.click':null,'chart.events.mousemove':null,'chart.tooltips':null,'chart.tooltips.highlight':true,'chart.tooltips.event':'onclick','chart.highlight.stroke':'rgba(0,0,0,0)','chart.highlight.fill':'rgba(255,255,255,0.7)','chart.linewidth':1,'chart.title':'','chart.title.background':null,'chart.title.hpos':null,'chart.title.vpos':null,'chart.title.font':null,'chart.title.size':null,'chart.title.color':null,'chart.title.bold':null,'chart.title.italic':null,'chart.title.x':null,'chart.title.y':null,'chart.title.halign':null,'chart.title.valign':null,'chart.xaxis.title':'','chart.xaxis.title.bold':null,'chart.xaxis.title.italic':null,'chart.xaxis.title.size':null,'chart.xaxis.title.font':null,'chart.xaxis.title.color':null,'chart.xaxis.title.x':null,'chart.xaxis.title.y':null,'chart.xaxis.title.pos':null,'chart.yaxis.title':'','chart.yaxis.title.bold':null,'chart.yaxis.title.size':null,'chart.yaxis.title.font':null,'chart.yaxis.title.color':null,'chart.yaxis.title.italic':null,'chart.yaxis.title.x':null,'chart.yaxis.title.y':null,'chart.yaxis.title.pos':null,'chart.clearto':'rgba(0,0,0,0)'}
if(!this.canvas){alert('[DRAWING.BACKGROUND] No canvas support');return;}
this.$0={};if(!this.canvas.__rgraph_aa_translated__){this.context.translate(0.5,0.5);this.canvas.__rgraph_aa_translated__=true;}
var RG=RGraph,ca=this.canvas,co=ca.getContext('2d'),prop=this.properties,pa=RG.Path,pa2=RG.path2,win=window,doc=document,ma=Math
if(RG.Effects&&typeof RG.Effects.decorate==='function'){RG.Effects.decorate(this);}
this.set=this.Set=function(name)
{var value=typeof arguments[1]==='undefined'?null:arguments[1];if(arguments.length===1&&typeof name==='object'){RG.parseObjectStyleConfig(this,name);return this;}
if(name.substr(0,6)!='chart.'){name='chart.'+name;}
while(name.match(/([A-Z])/)){name=name.replace(/([A-Z])/,'.'+RegExp.$1.toLowerCase());}
prop[name]=value;return this;};this.get=this.Get=function(name)
{if(name.substr(0,6)!='chart.'){name='chart.'+name;}
while(name.match(/([A-Z])/)){name=name.replace(/([A-Z])/,'.'+RegExp.$1.toLowerCase());}
return prop[name.toLowerCase()];};this.draw=this.Draw=function()
{RG.fireCustomEvent(this,'onbeforedraw');this.marginLeft=prop['chart.margin.left'];this.marginRight=prop['chart.margin.right'];this.marginTop=prop['chart.margin.top'];this.marginBottom=prop['chart.margin.bottom'];if(!this.colorsParsed){this.parseColors();this.colorsParsed=true;}
RG.drawBackgroundImage(this);RG.Background.draw(this);RG.installEventListeners(this);if(this.firstDraw){this.firstDraw=false;RG.fireCustomEvent(this,'onfirstdraw');this.firstDrawFunc();}
RG.fireCustomEvent(this,'ondraw');return this;};this.exec=function(func)
{func(this);return this;};this.getObjectByXY=function(e)
{if(this.getShape(e)){return this;}};this.getShape=function(e)
{var mouseXY=RG.getMouseXY(e),mouseX=mouseXY[0],mouseY=mouseXY[1];if(mouseX>=this.marginLeft&&mouseX<=(ca.width-this.marginRight)&&mouseY>=this.marginTop&&mouseY<=(ca.height-this.marginBottom)){var tooltip=prop['chart.tooltips']?prop['chart.tooltips'][0]:null
return{0:this,1:0,2:tooltip,'object':this,'index':0,'tooltip':tooltip};}
return null;};this.highlight=this.Highlight=function(shape)
{if(prop['chart.tooltips.highlight']){if(typeof prop['chart.highlight.style']==='function'){(prop['chart.highlight.style'])(shape);}else{pa2(co,'b r % % % % f % s %',prop['chart.margin.left'],prop['chart.margin.top'],ca.width-prop['chart.margin.left']-prop['chart.margin.right'],ca.height-prop['chart.margin.top']-prop['chart.margin.bottom'],prop['chart.highlight.fill'],prop['chart.highlight.stroke']);}}};this.parseColors=function()
{if(this.original_colors.length===0){this.original_colors['chart.background.color']=RG.arrayClone(prop['chart.background.color']);this.original_colors['chart.background.grid.color']=RG.arrayClone(prop['chart.background.grid.color']);this.original_colors['chart.highlight.stroke']=RG.arrayClone(prop['chart.highlight.stroke']);this.original_colors['chart.highlight.fill']=RG.arrayClone(prop['chart.highlight.fill']);}
prop['chart.background.color']=this.parseSingleColorForGradient(prop['chart.background.color']);prop['chart.background.grid.color']=this.parseSingleColorForGradient(prop['chart.background.grid.color']);prop['chart.highlight.stroke']=this.parseSingleColorForGradient(prop['chart.highlight.stroke']);prop['chart.highlight.fill']=this.parseSingleColorForGradient(prop['chart.highlight.fill']);};this.reset=function()
{};this.parseSingleColorForGradient=function(color)
{if(!color){return color;}
if(typeof color==='string'&&color.match(/^gradient\((.*)\)$/i)){if(color.match(/^gradient\(({.*})\)$/i)){return RGraph.parseJSONGradient({object:this,def:RegExp.$1});}
var parts=RegExp.$1.split(':'),grad=co.createLinearGradient(this.marginLeft,this.marginTop,ca.width-this.marginRight,ca.height-this.marginRight),diff=1/(parts.length-1);for(var j=0;j<parts.length;j+=1){grad.addColorStop(j*diff,RG.trim(parts[j]));}}
return grad?grad:color;};this.on=function(type,func)
{if(type.substr(0,2)!=='on'){type='on'+type;}
if(typeof this[type]!=='function'){this[type]=func;}else{RG.addCustomEventListener(this,type,func);}
return this;};this.firstDrawFunc=function()
{};RG.register(this);if(parseConfObjectForOptions){RG.parseObjectStyleConfig(this,conf.options);}};