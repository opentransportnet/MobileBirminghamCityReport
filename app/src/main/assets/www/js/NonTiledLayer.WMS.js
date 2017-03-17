/*
 * L.NonTiledLayer.WMS is used for putting WMS non tiled layers on the map.
 */
L.NonTiledLayer.WMS = L.NonTiledLayer.extend({
    defaultWmsParams: {
        service: 'WMS',
        request: 'GetMap',
        version: '1.1.1',
        layers: '',
        styles: '',
        format: 'image/jpeg',
        transparent: false
    },
    options: {
        crs: null,
        uppercase: false
    },
    initialize: function (url, options) { // (String, Object)
        this._wmsUrl = url;

        var wmsParams = L.extend({}, this.defaultWmsParams);

        // all keys that are not NonTiledLayer options go to WMS params
        for (var i in options) {
            if (!L.NonTiledLayer.prototype.options.hasOwnProperty(i) &&
                    !L.Layer.prototype.options.hasOwnProperty(i)) {
                wmsParams[i] = options[i];
            }
        }

        this.wmsParams = wmsParams;

        L.setOptions(this, options);
    },
    onAdd: function (map) {

        this._crs = this.options.crs || map.options.crs;
        this._wmsVersion = parseFloat(this.wmsParams.version);

        var projectionKey = this._wmsVersion >= 1.3 ? 'crs' : 'srs';
        this.wmsParams[projectionKey] = this._crs.code;

        L.NonTiledLayer.prototype.onAdd.call(this, map);

        map.on('click', this.getFeatureInfo, this);
    },
    onRemove: function (map) {
        // Triggered when the layer is removed from a map.
        //   Unregister a click listener, then do all the upstream WMS things
        L.NonTiledLayer.prototype.onRemove.call(this, map);
        map.off('click', this.getFeatureInfo, this);
    },
    getImageUrl: function (world1, world2, width, height) {
        var wmsParams = this.wmsParams;
        wmsParams.width = width;
        wmsParams.height = height;

        var nw = this._crs.project(world1);
        var se = this._crs.project(world2);

        var url = this._wmsUrl;

        var bbox = bbox = (this._wmsVersion >= 1.3 && this._crs === L.CRS.EPSG4326 ?
                [se.y, nw.x, nw.y, se.x] :
                [nw.x, se.y, se.x, nw.y]).join(',');

        return url +
                L.Util.getParamString(this.wmsParams, url, this.options.uppercase) +
                (this.options.uppercase ? '&BBOX=' : '&bbox=') + bbox;
    },
    setParams: function (params, noRedraw) {

        L.extend(this.wmsParams, params);

        if (!noRedraw) {
            this.redraw();
        }

        return this;
    },
    getFeatureInfo: function (evt) {
        // Make an AJAX request to the server and hope for the best
        var url = this.getFeatureInfoUrl(evt.latlng);
        var showResults = L.Util.bind(this.showGetFeatureInfo, this);

        var xmlHttp = new XMLHttpRequest();
        xmlHttp.onreadystatechange = function () {
            if (xmlHttp.readyState === XMLHttpRequest.DONE) {
                if (xmlHttp.status === 200) {
                    //console.log("[RESPONSE]" + xmlHttp.responseText);
                    var err = typeof xmlHttp.responseText === 'string' ? null : xmlHttp.responseText;
                    // showResults(err, evt.latlng, xmlHttp.responseText);

                    var position = xmlHttp.responseText.indexOf("issue_id = '") + 12;

                    var lastPosition = xmlHttp.responseText.indexOf("'", position);
                    var issueIdString = xmlHttp.responseText.slice(position, lastPosition);
                    var issueId = parseInt(issueIdString, 10);

                    if (issueId > 0) {
                        MainActivity.onIssueClick(issueId);
                    }
                }
                else if (xmlHttp.status === 400) {
                    console.log('There was an error 400');
                    //showResults(error);
                }
                else {
                    //showResults(error);
                    console.log('something else other than 200 was returned');
                }
            }
        };
        xmlHttp.open("GET", url, true);
        xmlHttp.send();
    },
    getFeatureInfoUrl: function (latlng) {
        // Construct a GetFeatureInfo request URL given a point
        var point = this._map.latLngToContainerPoint(latlng, this._map.getZoom()),
                size = this._map.getSize(),
                params = {
                    request: 'GetFeatureInfo',
                    service: 'WMS',
                    srs: 'EPSG:4326',
                    styles: this.wmsParams.styles,
                    transparent: this.wmsParams.transparent,
                    version: this.wmsParams.version,
                    format: this.wmsParams.format,
                    bbox: this._map.getBounds().toBBoxString(),
                    height: size.y,
                    width: size.x,
                    layers: this.wmsParams.layers,
                    query_layers: this.wmsParams.layers,
                    info_format: 'text/plain'
                };

        params[params.version === '1.3.0' ? 'i' : 'x'] = point.x;
        params[params.version === '1.3.0' ? 'j' : 'y'] = point.y;

        return this._wmsUrl + L.Util.getParamString(params, this._wmsUrl, true);
    },
    showGetFeatureInfo: function (err, latlng, content) {
        if (err) {
            console.log(err);
            return;
        } // do nothing if there's an error

        // Remove GetFeatureInfo part
        content = content.slice(25);

        if (content.indexOf("Search returned no results") > -1) {
            console.log("Search returned no results.");
            return;
        }

        // Otherwise show the content in a popup, or something.
        L.popup({maxWidth: 300})
                .setLatLng(latlng)
                .setContent(content)
                .openOn(this._map);
    }
});

L.nonTiledLayer.wms = function (url, options) {
    return new L.NonTiledLayer.WMS(url, options);
};