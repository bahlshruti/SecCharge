/**
 *  based, a little bit, on
 *  http://www.developer.nokia.com/Community/Wiki/Battery_JavaScript_component_for_Symbian_Web_Runtime
 */
var globalFullImage,batteryIsChargingImgSrc= 'assets/bat_charging.gif',batteryIsBlinkingSrc ="assets/battery-glow.gif",globalEmptyImage;
var globalFullBatterySrc,globalCriticalBatterySrc,isCharging=false;
function Battery(fullBatterySrc, emptlBatterySrc,criticalBatterySrc, batteryWidth, batteryHeight)
{
	globalFullBatterySrc = fullBatterySrc;
	globalCriticalBatterySrc = criticalBatterySrc;
    this.batteryHeight = batteryHeight;
    this.batteryWidth = batteryWidth;

    this.emptyBatteryElement = null;
    this.domElement = null;
	this.chargerConnected = false;
	this.init(fullBatterySrc, emptlBatterySrc);
	
}


Battery.prototype.init = function(fullBatterySrc, emptlBatterySrc)
{
    var el = document.createElement('div');
	el.style.marginLeft = '-40px';
	var fullImage = document.createElement('img');
	fullImage.id="backgroundImageEmpty";
	fullImage.style.position = 'absolute';
	
	fullImage.src = emptlBatterySrc;
	globalEmptyImage = fullImage;
	
	this.emptyBatteryElement = document.createElement('div');
	this.emptyBatteryElement.setAttribute("id","fullBatteryDiv");
    this.emptyBatteryElement.style.position = 'absolute';
    this.emptyBatteryElement.style.overflow = 'hidden';
   
   //this.emptyBatteryElement.style.width = this.batteryWidth + 'px';
    this.emptyBatteryElement.style.width = '0px';
	var emptyImage = document.createElement('img');
	/*Battery Image.*/
    emptyImage.src = fullBatterySrc;
	globalFullImage = emptyImage;
	this.emptyBatteryElement.appendChild(emptyImage);
	el.appendChild(fullImage);
    el.appendChild(this.emptyBatteryElement);
	this.domElement = el;
	
	//

	
}

/**
 * Updates battery with the given value (0 - 100 range)
 * @param {Number} value
 */
Battery.prototype.updateBattery = function(value)
{
	//value1=50;
	//var value = 100 - value1;
	
    if(value == undefined || value < 0)
        value = 0;

    if(!this.chargerConnected)
        this.batteryValue = value;
	console.log("Batter Width" + this.batteryWidth);
	var width = Math.round((( value) * this.batteryWidth) / 100);
	if(!isCharging) {
		if( value <= 25 ) {
			globalFullImage.src = globalCriticalBatterySrc;
		} else {
			globalFullImage.src = globalFullBatterySrc;
		}
	}
	console.log("value:" + value);
    console.log("width:" + width);
	
	
    this.emptyBatteryElement.style.width = width + 'px';
	
	
}


Battery.prototype.changeBatteryToChargingOrNot = function( value ) {
	isCharging = value;
console.log("Is Charinh"+value);
	if( value ) {
		console.log(value);
		console.log(globalFullImage.src);
		globalFullImage.src = batteryIsChargingImgSrc;
		globalEmptyImage.src = batteryIsBlinkingSrc;
		
	} else {
		console.log(value);
		console.log(globalFullImage.src);
		globalFullImage.src = globalFullBatterySrc;
		globalEmptyImage.src = "assets/bat_empty_light.png";
	}
}