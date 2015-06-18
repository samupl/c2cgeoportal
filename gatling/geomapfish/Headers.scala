package geomapfish

import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._

import java.lang.Math


class Headers extends Simulation {
    val random = new util.Random

    /*
     * CONFIG
     */

    val host = "geomapfish-demo.camptocamp.net"
    val theme = "Transport"
    val group = theme
    val lang = "fr"
    val raster_layers = "srtm,aster"

    val fts = "pau"

    val extent = Array(512691, 149736, 551491, 171836)
    val instance_id = "1.6"
    val resolution = 1
    val layer = "fuel"
    val rule = "fuel"

    val img_width = 2060
    val img_height = 1215

    val nbUser = 1
    val spaceTime = 2

    /*
     * /CONFIG
     */

    val rampTime = nbUser * spaceTime

    val cache_version = random.nextInt()
    val defaults_feeder = Array(Map(
        "instance_id" -> instance_id,
        "theme" -> theme,
        "group" -> group,
        "lang" ->lang,
        "cache_version" -> cache_version
    )).random

    def randX: Int = extent.get(0) + random.nextInt(extent.get(2) - extent.get(0))
    def randY: Int = extent.get(1) + random.nextInt(extent.get(3) - extent.get(1))
    def getBbox(width: Int, height: Int)(x: Int, y: Int, resolution: Double): String = {
        val x2 = x + width * resolution
        val y2 = y + height * resolution
        "%d,%d,%f,%f".format(x, y, x2, y2)
    }
    val common_bbox = getBbox(img_width, img_height)_

    val httpProtocol = http
        .baseURL("http://" + host)
        .inferHtmlResources(WhiteList("http://" + host + "/*"), BlackList())
        .acceptHeader("image/png,image/*;q=0.8,*/*;q=0.5")
        .acceptEncodingHeader("gzip, deflate")
        .acceptLanguageHeader(lang + ";q=0.5")
        .connection("keep-alive")
        .contentTypeHeader("application/x-www-form-urlencoded; charset=UTF-8")
        .userAgentHeader("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:38.0) Gecko/20100101 Firefox/38.0")

    val headers = Map(
        "Accept" -> "*/*"
    )
    val headers_xml = Map(
        "Accept" -> "*/*",
        "Content-Type" -> "application/xml"
    )
    val headers_json = Map(
        "Accept" -> "*/*",
        "Content-Type" -> "application/json; charset=UTF-8"
    )

    val uri = "http://" + host + "/"

    val scn = scenario("HeadersTests")
        .feed(defaults_feeder)
        .exec(
            http("WMS GetCapabilities")
            .get("/${instance_id}/wsgi/mapserv_proxy")
            .queryParam("cache_version", cache_version)
            .queryParam("SERVICE", "WMS")
            .queryParam("VERSION", "1.1.1")
            .queryParam("REQUEST", "GetCapabilities")
            .headers(headers)
            .check(status.is(200))
            .check(headerRegex("Access-Control-Allow-Origin", "^\\*$"))
            .check(headerRegex("Access-Control-Allow-Headers", "X-Requested-With"))
            .check(headerRegex("Access-Control-Allow-Headers", "Content-Type"))
            .check(headerRegex("Content-Encoding", "gzip"))
            .check(headerRegex("Cache-Control", "max-age=864000, public")))
        .pause(1)
        .exec(
            http("WMS GetMap")
            .get("/${instance_id}/wsgi/mapserv_proxy")
            .queryParam("cache_version", cache_version)
            .queryParam("SERVICE", "WMS")
            .queryParam("VERSION", "1.1.1")
            .queryParam("REQUEST", "GetMap")
            .queryParam("FORMAT", "image/png")
            .queryParam("TRANSPARENT", "TRUE")
            .queryParam("LAYERS", layer)
            .queryParam("STYLES", "")
            .queryParam("SRS", "EPSG:21781")
            .queryParam("BBOX", common_bbox(randX, randY, resolution))
            .queryParam("WIDTH", img_width)
            .queryParam("HEIGHT", img_height)
            .headers(headers)
            .check(status.is(200))
            .check(headerRegex("Access-Control-Allow-Origin", "^\\*$"))
            .check(headerRegex("Access-Control-Allow-Headers", "X-Requested-With"))
            .check(headerRegex("Access-Control-Allow-Headers", "Content-Type"))
            .check(headerRegex("Cache-Control", "no-cache")))
        .pause(1)
        .exec(
            http("WMS GetFeatureInfo")
            .get("/${instance_id}/wsgi/mapserv_proxy")
            .queryParam("cache_version", cache_version)
            .queryParam("SERVICE", "WMS")
            .queryParam("VERSION", "1.1.1")
            .queryParam("REQUEST", "GetFeatureInfo")
            .queryParam("FORMAT", "image/png")
            .queryParam("TRANSPARENT", "TRUE")
            .queryParam("LAYERS", layer)
            .queryParam("QUERY_LAYERS", layer)
            .queryParam("STYLES", "")
            .queryParam("SRS", "EPSG:21781")
            .queryParam("FEATURE_COUNT", 200)
            .queryParam("INFO_FORMAT", "application/vnd.ogc.gml")
            .queryParam("X", 517)
            .queryParam("Y", 224)
            .queryParam("BBOX", common_bbox(randX, randY, resolution))
            .queryParam("WIDTH", img_width)
            .queryParam("HEIGHT", img_height)
            .headers(headers)
            .check(status.is(200))
            .check(headerRegex("Access-Control-Allow-Origin", "^\\*$"))
            .check(headerRegex("Access-Control-Allow-Headers", "X-Requested-With"))
            .check(headerRegex("Access-Control-Allow-Headers", "Content-Type"))
            .check(headerRegex("Content-Encoding", "gzip"))
            .check(headerRegex("Cache-Control", "no-cache")))
        .pause(1)
        .exec(
            http("WMS GetLegendGraphic")
            .get("/${instance_id}/wsgi/mapserv_proxy")
            .queryParam("cache_version", cache_version)
            .queryParam("SERVICE", "WMS")
            .queryParam("VERSION", "1.1.1")
            .queryParam("REQUEST", "GetLegendGraphic")
            .queryParam("FORMAT", "image/png")
            .queryParam("TRANSPARENT", "TRUE")
            .queryParam("LAYER", layer)
            .queryParam("STYLES", "")
            .queryParam("SRS", "EPSG:21781")
            .headers(headers)
            .check(status.is(200))
            .check(headerRegex("Access-Control-Allow-Origin", "^\\*$"))
            .check(headerRegex("Access-Control-Allow-Headers", "X-Requested-With"))
            .check(headerRegex("Access-Control-Allow-Headers", "Content-Type"))
            .check(headerRegex("Cache-Control", "max-age=864000, public")))
        .pause(1)
        .exec(
            http("WMS GetLegendGraphic Rule")
            .get("/${instance_id}/wsgi/mapserv_proxy")
            .queryParam("cache_version", cache_version)
            .queryParam("SERVICE", "WMS")
            .queryParam("VERSION", "1.1.1")
            .queryParam("REQUEST", "GetLegendGraphic")
            .queryParam("FORMAT", "image/png")
            .queryParam("TRANSPARENT", "TRUE")
            .queryParam("LAYER", layer)
            .queryParam("RULE", rule)
            .queryParam("STYLES", "")
            .queryParam("SRS", "EPSG:21781")
            .headers(headers)
            .check(status.is(200))
            .check(headerRegex("Access-Control-Allow-Origin", "^\\*$"))
            .check(headerRegex("Access-Control-Allow-Headers", "X-Requested-With"))
            .check(headerRegex("Access-Control-Allow-Headers", "Content-Type"))
            .check(headerRegex("Cache-Control", "max-age=864000, public")))
        .pause(1)
        .exec(
            http("WMS DescribeLayer")
            .get("/${instance_id}/wsgi/mapserv_proxy")
            .queryParam("cache_version", cache_version)
            .queryParam("SERVICE", "WMS")
            .queryParam("VERSION", "1.1.1")
            .queryParam("REQUEST", "DescribeLayer")
            .queryParam("LAYERS", layer)
            .headers(headers)
            .check(status.is(200))
            .check(headerRegex("Access-Control-Allow-Origin", "^\\*$"))
            .check(headerRegex("Access-Control-Allow-Headers", "X-Requested-With"))
            .check(headerRegex("Access-Control-Allow-Headers", "Content-Type"))
            .check(headerRegex("Content-Encoding", "gzip"))
            .check(headerRegex("Cache-Control", "max-age=864000, public")))
        .pause(1)
        .exec(
            http("WFS GetCapabilities")
            .get("/${instance_id}/wsgi/mapserv_proxy")
            .queryParam("cache_version", cache_version)
            .queryParam("SERVICE", "WFS")
            .queryParam("VERSION", "1.1.0")
            .queryParam("REQUEST", "GetCapabilities")
            .headers(headers)
            .check(status.is(200))
            .check(headerRegex("Access-Control-Allow-Origin", "^\\*$"))
            .check(headerRegex("Access-Control-Allow-Headers", "X-Requested-With"))
            .check(headerRegex("Access-Control-Allow-Headers", "Content-Type"))
            .check(headerRegex("Content-Encoding", "gzip"))
            .check(headerRegex("Cache-Control", "max-age=864000, public")))
        .pause(1)
        .exec(
            http("WFS GetFeature")
            .post("/${instance_id}/wsgi/mapserv_proxy")
            .queryParam("cache_version", cache_version)
            .body(StringBody("""<wfs:GetFeature xmlns:wfs="http://www.opengis.net/wfs" service="WFS" version="1.1.0" maxFeatures="200" xsi:schemaLocation="http://www.opengis.net/wfs http://schemas.opengis.net/wfs/1.1.0/wfs.xsd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"><wfs:Query typeName="feature:zoo" xmlns:feature="http://mapserver.gis.umn.edu/mapserver"><ogc:Filter xmlns:ogc="http://www.opengis.net/ogc"><ogc:BBOX><ogc:PropertyName>geom</ogc:PropertyName><gml:Envelope xmlns:gml="http://www.opengis.net/gml"><gml:lowerCorner>538875 152070</gml:lowerCorner><gml:upperCorner>539235 152410</gml:upperCorner></gml:Envelope></ogc:BBOX></ogc:Filter></wfs:Query></wfs:GetFeature>"""))
            .headers(headers_xml)
            .check(status.is(200))
            .check(headerRegex("Access-Control-Allow-Origin", "^\\*$"))
            .check(headerRegex("Access-Control-Allow-Headers", "X-Requested-With"))
            .check(headerRegex("Access-Control-Allow-Headers", "Content-Type"))
            .check(headerRegex("Content-Encoding", "gzip"))
            .check(headerRegex("Cache-Control", "no-cache")))
        .pause(1)
        .exec(
            http("WFS DescribeFeatureType")
            .get("/${instance_id}/wsgi/mapserv_proxy")
            .queryParam("cache_version", cache_version)
            .queryParam("SERVICE", "WFS")
            .queryParam("VERSION", "1.1.0")
            .queryParam("REQUEST", "DescribeFeatureType")
            .queryParam("TYPENAME", layer)
            .headers(headers)
            .check(status.is(200))
            .check(headerRegex("Access-Control-Allow-Origin", "^\\*$"))
            .check(headerRegex("Access-Control-Allow-Headers", "X-Requested-With"))
            .check(headerRegex("Access-Control-Allow-Headers", "Content-Type"))
            .check(headerRegex("Content-Encoding", "gzip"))
            .check(headerRegex("Cache-Control", "max-age=864000, public")))
        .pause(1)
        .exec(
            http("WMTS capabilities")
            .get("/${instance_id}/tiles/1.0.0/WMTSCapabilities.xml")
            .headers(headers)
            .check(status.is(200))
            .check(headerRegex("Access-Control-Allow-Origin", "^\\*$"))
            .check(headerRegex("Access-Control-Allow-Headers", "X-Requested-With"))
            .check(headerRegex("Access-Control-Allow-Headers", "Content-Type"))
            .check(headerRegex("Content-Encoding", "gzip"))
            .check(headerRegex("Cache-Control", "max-age=864000, public")))
        .pause(1)
        .exec(
            http("WMTS tile")
            .get("/${instance_id}/tiles/1.0.0/plan/default/0_1/swissgrid/0/0/0.png")
            .headers(headers)
            .check(status.is(200))
            .check(headerRegex("Access-Control-Allow-Origin", "^\\*$"))
            .check(headerRegex("Access-Control-Allow-Headers", "X-Requested-With"))
            .check(headerRegex("Access-Control-Allow-Headers", "Content-Type"))
            .check(headerRegex("Cache-Control", "max-age=864000, public")))
        .pause(1)
        .exec(
            http("Print3 capabilities.json")
            .get("/${instance_id}/wsgi/printproxy/capabilities.json")
            .headers(headers)
            .check(status.is(200))
            .check(headerRegex("Access-Control-Allow-Origin", "^\\*$"))
            .check(headerRegex("Access-Control-Allow-Headers", "X-Requested-With"))
            .check(headerRegex("Access-Control-Allow-Headers", "Content-Type"))
            .check(headerRegex("Content-Encoding", "gzip"))
            .check(headerRegex("Cache-Control", "max-age=864000, public")))
        .pause(1)
        .exec(
            http("Print3 capabilities.json")
            .options("/${instance_id}/wsgi/printproxy/capabilities.json")
            .headers(headers)
            .check(status.is(200))
            .check(headerRegex("Access-Control-Allow-Origin", "^\\*$"))
            .check(headerRegex("Access-Control-Allow-Headers", "X-Requested-With"))
            .check(headerRegex("Access-Control-Allow-Headers", "Content-Type"))
            .check(headerRegex("Content-Encoding", "gzip"))
            .check(headerRegex("Cache-Control", "max-age=864000, public")))
        .pause(1)
        .exec(
            http("Print3 report.pdf")
            .post("/${instance_id}/wsgi/printproxy/report.pdf")
            .body(StringBody("""{"layout":"A4 portrait","outputFormat":"pdf","attributes":{"title":"","comments":"","debug":"0","datasource":[],"map":{"projection":"EPSG:21781","dpi":254,"rotation":0,"center":[542000,154000],"scale":50000,"longitudeFirst":true,"layers":[],"legend": {}}}}"""))
            .headers(headers_json)
            .check(status.is(200))
            .check(headerRegex("Access-Control-Allow-Origin", "^\\*$"))
            .check(headerRegex("Access-Control-Allow-Headers", "X-Requested-With"))
            .check(headerRegex("Access-Control-Allow-Headers", "Content-Type"))
            .check(headerRegex("Content-Encoding", "gzip"))
            .check(headerRegex("Cache-Control", "no-cache")))
        .pause(1)
        .exec(
            http("Print3 report.pdf")
            .options("/${instance_id}/wsgi/printproxy/report.pdf")
            .headers(headers_json)
            .check(status.is(200))
            .check(headerRegex("Access-Control-Allow-Origin", "^\\*$"))
            .check(headerRegex("Access-Control-Allow-Headers", "X-Requested-With"))
            .check(headerRegex("Access-Control-Allow-Headers", "Content-Type"))
            .check(headerRegex("Content-Encoding", "gzip"))
            .check(headerRegex("Cache-Control", "no-cache")))
        .pause(1)
        // TODO manually Print status
        // TODO manually Print get file
        .exec(
            http("FullTextSearch")
            .get("/${instance_id}/wsgi/fulltextsearch")
            .queryParam("query", fts)
            .headers(headers)
            .check(status.is(200))
            .check(headerRegex("Content-Encoding", "gzip"))
            .check(headerRegex("Cache-Control", "no-cache")))
        .pause(1)
        .exec(
            http("FullTextSearch callback")
            .get("/${instance_id}/wsgi/fulltextsearch")
            .queryParam("limit", 20)
            .queryParam("query", fts)
            .queryParam("callback", "stcCallback1003")
            .headers(headers)
            .check(status.is(200))
            .check(headerRegex("Content-Encoding", "gzip"))
            .check(headerRegex("Cache-Control", "no-cache")))
        .pause(1)
        .exec(
            http("Raster")
            .get("/${instance_id}/wsgi/raster")
            .queryParam("lon", 561030)
            .queryParam("lat", 143010)
            .headers(headers)
            .check(status.is(200))
            // .check(headerRegex("Content-Encoding", "gzip")) // Apache don't seam to compress small files
            .check(headerRegex("Cache-Control", "no-cache")))
        .pause(1)
        .exec(
            http("Raster callback")
            .get("/${instance_id}/wsgi/raster")
            .queryParam("lon", 561030)
            .queryParam("lat", 143010)
            .queryParam("callback", "stcCallback1003")
            .headers(headers)
            .check(status.is(200))
            // .check(headerRegex("Content-Encoding", "gzip")) // Apache don't seam to compress small files
            .check(headerRegex("Cache-Control", "no-cache")))
        .pause(1)
        .exec(
            http("Profile")
            .post("/${instance_id}/wsgi/profile.json")
            .formParam("layers", raster_layers)
            .formParam("geom", """{"type":"LineString","coordinates":[[559570,143590],[560870,142570]]}""")
            .formParam("nbPoints", "100")
            .headers(headers)
            .check(status.is(200))
            .check(headerRegex("Content-Encoding", "gzip"))
            .check(headerRegex("Cache-Control", "no-cache")))
        .pause(1)
        .exec(
            http("Profile callback")
            .post("/${instance_id}/wsgi/profile.json")
            .queryParam("callback", "stcCallback1003")
            .formParam("layers", raster_layers)
            .formParam("geom", """{"type":"LineString","coordinates":[[559570,143590],[560870,142570]]}""")
            .formParam("nbPoints", "100")
            .headers(headers)
            .check(status.is(200))
            .check(headerRegex("Content-Encoding", "gzip"))
            .check(headerRegex("Cache-Control", "no-cache")))
        .pause(1)
        .exec(
            http("Short")
            .post("/${instance_id}/wsgi/short/create")
            .formParam("url", uri + "${instance_id}/theme/toto")
            .headers(headers)
            .check(status.is(200))
            .check(headerRegex("Content-Encoding", "gzip"))
            .check(headerRegex("Cache-Control", "no-cache")))
        .pause(1)
        .exec(http("Index").get("/${instance_id}")
            .headers(headers)
            .check(status.is(200))
            .check(headerRegex("Content-Encoding", "gzip"))
            .check(headerRegex("Cache-Control", "no-cache")))
        .pause(1)
        .exec(http("Viewer").get("/${instance_id}/wsgi/viewer.js")
            .headers(headers)
            .check(status.is(200))
            .check(headerRegex("Content-Encoding", "gzip"))
            .check(headerRegex("Cache-Control", "max-age=864000, public")))
        .pause(1)
        .exec(http("Theme").get("/${instance_id}/wsgi/themes")
            .headers(headers)
            .check(status.is(200))
            .check(headerRegex("Content-Encoding", "gzip"))
            .check(headerRegex("Cache-Control", "max-age=864000, public")))
        .pause(1)
        .exec(http("Mobile index").get("/${instance_id}/mobile")
            .headers(headers)
            .check(status.is(200))
            .check(headerRegex("Content-Encoding", "gzip"))
            .check(headerRegex("Cache-Control", "no-cache")))
        .pause(1)
        .exec(http("Mobile config.js").get("/${instance_id}/mobile/config.js")
            .headers(headers)
            .check(status.is(200))
            .check(headerRegex("Content-Encoding", "gzip"))
            .check(headerRegex("Cache-Control", "max-age=864000, public")))
        .pause(1)
        .exec(http("api.js").get("/${instance_id}/wsgi/api.js")
            .headers(headers)
            .check(status.is(200))
            .check(headerRegex("Content-Encoding", "gzip"))
            .check(headerRegex("Cache-Control", "no-cache")))
        .pause(1)
        .exec(http("Xapi.js").get("/${instance_id}/wsgi/xapi.js")
            .headers(headers)
            .check(status.is(200))
            .check(headerRegex("Content-Encoding", "gzip"))
            .check(headerRegex("Cache-Control", "no-cache")))
        .pause(1)
        .exec(http("Static").get("/${instance_id}/wsgi/proj/${cache_version}/build/app.js")
            .headers(headers)
            .check(status.is(200))
            .check(headerRegex("Content-Encoding", "gzip"))
            .check(headerRegex("Cache-Control", "max-age=864000"))
            .check(headerRegex("Cache-Control", "public")))
        .pause(1)

        .exec(http("Login")
            .post("/${instance_id}/wsgi/login")
            .formParam("login", "demo")
            .formParam("password", "demo")
            .formParam("newPassword", "")
            .formParam("confirmNewPassword", "")
            .headers(headers)
            .check(status.is(200))
            .check(headerRegex("Cache-Control", "no-cache")))
        .pause(1)
        .exec(http("Login index")
            .post("/${instance_id}/theme/${theme}")
            .formParam("login", "demo")
            .formParam("password", "demo")
            .formParam("newPassword", "")
            .formParam("confirmNewPassword", "")
            .headers(headers)
            .check(status.is(200))
            .check(headerRegex("Content-Encoding", "gzip"))
            .check(headerRegex("Cache-Control", "no-cache")))
        .pause(1)

        .exec(
            http("WMS GetCapabilities")
            .get("/${instance_id}/wsgi/mapserv_proxy")
            .queryParam("cache_version", cache_version)
            .queryParam("role", "demo")
            .queryParam("SERVICE", "WMS")
            .queryParam("VERSION", "1.1.1")
            .queryParam("REQUEST", "GetCapabilities")
            .headers(headers)
            .check(status.is(200))
            .check(headerRegex("Access-Control-Allow-Origin", "^\\*$"))
            .check(headerRegex("Access-Control-Allow-Headers", "X-Requested-With"))
            .check(headerRegex("Access-Control-Allow-Headers", "Content-Type"))
            .check(headerRegex("Content-Encoding", "gzip"))
            .check(headerRegex("Cache-Control", "max-age=864000, private")))
        .pause(1)
        .exec(
            http("WMS GetMap")
            .get("/${instance_id}/wsgi/mapserv_proxy")
            .queryParam("cache_version", cache_version)
            .queryParam("SERVICE", "WMS")
            .queryParam("VERSION", "1.1.1")
            .queryParam("REQUEST", "GetMap")
            .queryParam("FORMAT", "image/png")
            .queryParam("TRANSPARENT", "TRUE")
            .queryParam("LAYERS", layer)
            .queryParam("STYLES", "")
            .queryParam("SRS", "EPSG:21781")
            .queryParam("BBOX", common_bbox(randX, randY, resolution))
            .queryParam("WIDTH", img_width)
            .queryParam("HEIGHT", img_height)
            .headers(headers)
            .check(status.is(200))
            .check(headerRegex("Access-Control-Allow-Origin", "^\\*$"))
            .check(headerRegex("Access-Control-Allow-Headers", "X-Requested-With"))
            .check(headerRegex("Access-Control-Allow-Headers", "Content-Type"))
            .check(headerRegex("Cache-Control", "no-cache")))
        .pause(1)
        .exec(
            http("WMS GetFeatureInfo")
            .get("/${instance_id}/wsgi/mapserv_proxy")
            .queryParam("cache_version", cache_version)
            .queryParam("SERVICE", "WMS")
            .queryParam("VERSION", "1.1.1")
            .queryParam("REQUEST", "GetFeatureInfo")
            .queryParam("FORMAT", "image/png")
            .queryParam("TRANSPARENT", "TRUE")
            .queryParam("LAYERS", layer)
            .queryParam("QUERY_LAYERS", layer)
            .queryParam("STYLES", "")
            .queryParam("SRS", "EPSG:21781")
            .queryParam("FEATURE_COUNT", 200)
            .queryParam("INFO_FORMAT", "application/vnd.ogc.gml")
            .queryParam("X", 517)
            .queryParam("Y", 224)
            .queryParam("BBOX", common_bbox(randX, randY, resolution))
            .queryParam("WIDTH", img_width)
            .queryParam("HEIGHT", img_height)
            .headers(headers)
            .check(status.is(200))
            .check(headerRegex("Access-Control-Allow-Origin", "^\\*$"))
            .check(headerRegex("Access-Control-Allow-Headers", "X-Requested-With"))
            .check(headerRegex("Access-Control-Allow-Headers", "Content-Type"))
            .check(headerRegex("Content-Encoding", "gzip"))
            .check(headerRegex("Cache-Control", "no-cache")))
        .pause(1)
        .exec(
            http("WMS GetLegendGraphic")
            .get("/${instance_id}/wsgi/mapserv_proxy")
            .queryParam("cache_version", cache_version)
            .queryParam("SERVICE", "WMS")
            .queryParam("VERSION", "1.1.1")
            .queryParam("REQUEST", "GetLegendGraphic")
            .queryParam("FORMAT", "image/png")
            .queryParam("TRANSPARENT", "TRUE")
            .queryParam("LAYER", layer)
            .queryParam("STYLES", "")
            .queryParam("SRS", "EPSG:21781")
            .headers(headers)
            .check(status.is(200))
            .check(headerRegex("Access-Control-Allow-Origin", "^\\*$"))
            .check(headerRegex("Access-Control-Allow-Headers", "X-Requested-With"))
            .check(headerRegex("Access-Control-Allow-Headers", "Content-Type"))
            .check(headerRegex("Cache-Control", "max-age=864000, public")))
        .pause(1)
        .exec(
            http("WMS GetLegendGraphic Rule")
            .get("/${instance_id}/wsgi/mapserv_proxy")
            .queryParam("cache_version", cache_version)
            .queryParam("SERVICE", "WMS")
            .queryParam("VERSION", "1.1.1")
            .queryParam("REQUEST", "GetLegendGraphic")
            .queryParam("FORMAT", "image/png")
            .queryParam("TRANSPARENT", "TRUE")
            .queryParam("LAYER", layer)
            .queryParam("RULE", rule)
            .queryParam("STYLES", "")
            .queryParam("SRS", "EPSG:21781")
            .headers(headers)
            .check(status.is(200))
            .check(headerRegex("Access-Control-Allow-Origin", "^\\*$"))
            .check(headerRegex("Access-Control-Allow-Headers", "X-Requested-With"))
            .check(headerRegex("Access-Control-Allow-Headers", "Content-Type"))
            .check(headerRegex("Cache-Control", "max-age=864000, public")))
        .pause(1)
        .exec(
            http("WMS DescribeLayer")
            .get("/${instance_id}/wsgi/mapserv_proxy")
            .queryParam("cache_version", cache_version)
            .queryParam("role", "demo")
            .queryParam("SERVICE", "WMS")
            .queryParam("VERSION", "1.1.1")
            .queryParam("REQUEST", "DescribeLayer")
            .queryParam("LAYERS", layer)
            .headers(headers)
            .check(status.is(200))
            .check(headerRegex("Access-Control-Allow-Origin", "^\\*$"))
            .check(headerRegex("Access-Control-Allow-Headers", "X-Requested-With"))
            .check(headerRegex("Access-Control-Allow-Headers", "Content-Type"))
            .check(headerRegex("Content-Encoding", "gzip"))
            .check(headerRegex("Cache-Control", "max-age=864000, private")))
        .pause(1)
        .exec(
            http("WFS GetCapabilities")
            .get("/${instance_id}/wsgi/mapserv_proxy")
            .queryParam("cache_version", cache_version)
            .queryParam("role", "demo")
            .queryParam("SERVICE", "WFS")
            .queryParam("VERSION", "1.1.0")
            .queryParam("REQUEST", "GetCapabilities")
            .headers(headers)
            .check(status.is(200))
            .check(headerRegex("Access-Control-Allow-Origin", "^\\*$"))
            .check(headerRegex("Access-Control-Allow-Headers", "X-Requested-With"))
            .check(headerRegex("Access-Control-Allow-Headers", "Content-Type"))
            .check(headerRegex("Content-Encoding", "gzip"))
            .check(headerRegex("Cache-Control", "max-age=864000, private")))
        .pause(1)
        .exec(
            http("WFS GetFeature")
            .post("/${instance_id}/wsgi/mapserv_proxy")
            .queryParam("cache_version", cache_version)
            .body(StringBody("""<wfs:GetFeature xmlns:wfs="http://www.opengis.net/wfs" service="WFS" version="1.1.0" maxFeatures="200" xsi:schemaLocation="http://www.opengis.net/wfs http://schemas.opengis.net/wfs/1.1.0/wfs.xsd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"><wfs:Query typeName="feature:zoo" xmlns:feature="http://mapserver.gis.umn.edu/mapserver"><ogc:Filter xmlns:ogc="http://www.opengis.net/ogc"><ogc:BBOX><ogc:PropertyName>geom</ogc:PropertyName><gml:Envelope xmlns:gml="http://www.opengis.net/gml"><gml:lowerCorner>538875 152070</gml:lowerCorner><gml:upperCorner>539235 152410</gml:upperCorner></gml:Envelope></ogc:BBOX></ogc:Filter></wfs:Query></wfs:GetFeature>"""))
            .headers(headers_xml)
            .check(status.is(200))
            .check(headerRegex("Access-Control-Allow-Origin", "^\\*$"))
            .check(headerRegex("Access-Control-Allow-Headers", "X-Requested-With"))
            .check(headerRegex("Access-Control-Allow-Headers", "Content-Type"))
            .check(headerRegex("Content-Encoding", "gzip"))
            .check(headerRegex("Cache-Control", "no-cache")))
        .pause(1)
        .exec(
            http("WFS DescribeFeatureType")
            .get("/${instance_id}/wsgi/mapserv_proxy")
            .queryParam("cache_version", cache_version)
            .queryParam("role", "demo")
            .queryParam("SERVICE", "WFS")
            .queryParam("VERSION", "1.1.0")
            .queryParam("REQUEST", "DescribeFeatureType")
            .queryParam("TYPENAME", layer)
            .headers(headers)
            .check(status.is(200))
            .check(headerRegex("Access-Control-Allow-Origin", "^\\*$"))
            .check(headerRegex("Access-Control-Allow-Headers", "X-Requested-With"))
            .check(headerRegex("Access-Control-Allow-Headers", "Content-Type"))
            .check(headerRegex("Content-Encoding", "gzip"))
            .check(headerRegex("Cache-Control", "max-age=864000, private")))
        .pause(1)
        .exec(
            http("WMTS capabilities")
            .get("/${instance_id}/tiles/1.0.0/WMTSCapabilities.xml")
            .headers(headers)
            .check(status.is(200))
            .check(headerRegex("Access-Control-Allow-Origin", "^\\*$"))
            .check(headerRegex("Access-Control-Allow-Headers", "X-Requested-With"))
            .check(headerRegex("Access-Control-Allow-Headers", "Content-Type"))
            .check(headerRegex("Content-Encoding", "gzip"))
            .check(headerRegex("Cache-Control", "max-age=864000, public")))
        .pause(1)
        .exec(
            http("WMTS tile")
            .get("/${instance_id}/tiles/1.0.0/plan/default/0_1/swissgrid/0/0/0.png")
            .headers(headers)
            .check(status.is(200))
            .check(headerRegex("Access-Control-Allow-Origin", "^\\*$"))
            .check(headerRegex("Access-Control-Allow-Headers", "X-Requested-With"))
            .check(headerRegex("Access-Control-Allow-Headers", "Content-Type"))
            .check(headerRegex("Cache-Control", "max-age=864000, public")))
        .pause(1)
        .exec(
            http("Print3 capabilities.json")
            .get("/${instance_id}/wsgi/printproxy/capabilities.json")
            .queryParam("role", "demo")
            .headers(headers)
            .check(status.is(200))
            .check(headerRegex("Content-Encoding", "gzip"))
            .check(headerRegex("Cache-Control", "max-age=864000, private")))
        .pause(1)
        .exec(
            http("Print3 capabilities.json")
            .options("/${instance_id}/wsgi/printproxy/capabilities.json")
            .queryParam("role", "demo")
            .headers(headers)
            .check(status.is(200))
            .check(headerRegex("Content-Encoding", "gzip"))
            .check(headerRegex("Cache-Control", "max-age=864000, private")))
        .pause(1)
        .exec(
            http("Print3 report.pdf")
            .post("/${instance_id}/wsgi/printproxy/report.pdf")
            .body(StringBody("""{"layout":"A4 portrait","outputFormat":"pdf","attributes":{"title":"","comments":"","debug":"0","datasource":[],"map":{"projection":"EPSG:21781","dpi":254,"rotation":0,"center":[542000,154000],"scale":50000,"longitudeFirst":true,"layers":[],"legend": {}}}}"""))
            .headers(headers_json)
            .check(status.is(200))
            .check(headerRegex("Content-Encoding", "gzip"))
            .check(headerRegex("Cache-Control", "no-cache")))
        .pause(1)
        .exec(
            http("Print3 report.pdf")
            .options("/${instance_id}/wsgi/printproxy/report.pdf")
            .headers(headers_json)
            .check(status.is(200))
            .check(headerRegex("Content-Encoding", "gzip"))
            .check(headerRegex("Cache-Control", "no-cache")))
        .pause(1)
        // TODO manually Print status
        // TODO manually Print get file
        .exec(
            http("FullTextSearch")
            .get("/${instance_id}/wsgi/fulltextsearch")
            .queryParam("limit", 20)
            .queryParam("query", fts)
            .headers(headers)
            .check(status.is(200))
            .check(headerRegex("Content-Encoding", "gzip"))
            .check(headerRegex("Cache-Control", "no-cache")))
        .pause(1)
        .exec(
            http("FullTextSearch callback")
            .get("/${instance_id}/wsgi/fulltextsearch")
            .queryParam("limit", 20)
            .queryParam("query", fts)
            .queryParam("callback", "stcCallback1003")
            .headers(headers)
            .check(status.is(200))
            .check(headerRegex("Content-Encoding", "gzip"))
            .check(headerRegex("Cache-Control", "no-cache")))
        .pause(1)
        .exec(
            http("Raster")
            .get("/${instance_id}/wsgi/raster")
            .queryParam("lon", 561030)
            .queryParam("lat", 143010)
            .headers(headers)
            .check(status.is(200))
            // .check(headerRegex("Content-Encoding", "gzip")) // Apache don't seam to compress small files
            .check(headerRegex("Cache-Control", "no-cache")))
        .pause(1)
        .exec(
            http("Raster callback")
            .get("/${instance_id}/wsgi/raster")
            .queryParam("lon", 561030)
            .queryParam("lat", 143010)
            .queryParam("callback", "stcCallback1003")
            .headers(headers)
            .check(status.is(200))
            // .check(headerRegex("Content-Encoding", "gzip")) // Apache don't seam to compress small files
            .check(headerRegex("Cache-Control", "no-cache")))
        .pause(1)
        .exec(
            http("Profile")
            .post("/${instance_id}/wsgi/profile.json")
            .formParam("layers", raster_layers)
            .formParam("geom", """{"type":"LineString","coordinates":[[559570,143590],[560870,142570]]}""")
            .formParam("nbPoints", "100")
            .headers(headers)
            .check(status.is(200))
            .check(headerRegex("Content-Encoding", "gzip"))
            .check(headerRegex("Cache-Control", "no-cache")))
        .pause(1)
        .exec(
            http("Profile callback")
            .post("/${instance_id}/wsgi/profile.json")
            .queryParam("callback", "stcCallback1003")
            .formParam("layers", raster_layers)
            .formParam("geom", """{"type":"LineString","coordinates":[[559570,143590],[560870,142570]]}""")
            .formParam("nbPoints", "100")
            .headers(headers)
            .check(status.is(200))
            .check(headerRegex("Content-Encoding", "gzip"))
            .check(headerRegex("Cache-Control", "no-cache")))
        .pause(1)
        .exec(
            http("Short")
            .post("/${instance_id}/wsgi/short/create")
            .formParam("url", uri + "${instance_id}/theme/toto")
            .headers(headers)
            .check(status.is(200))
            .check(headerRegex("Content-Encoding", "gzip"))
            .check(headerRegex("Cache-Control", "no-cache")))
        .pause(1)
        .exec(http("Index").get("/${instance_id}")
            .headers(headers)
            .check(status.is(200))
            .check(headerRegex("Cache-Control", "no-cache")))
        .pause(1)
        .exec(http("Viewer").get("/${instance_id}/wsgi/viewer.js")
            .queryParam("role", "demo")
            .headers(headers)
            .check(status.is(200))
            .check(headerRegex("Content-Encoding", "gzip"))
            .check(headerRegex("Cache-Control", "max-age=864000, private")))
        .pause(1)
        .exec(http("Theme").get("/${instance_id}/wsgi/themes")
            .queryParam("role", "demo")
            .headers(headers)
            .check(status.is(200))
            .check(headerRegex("Content-Encoding", "gzip"))
            .check(headerRegex("Cache-Control", "max-age=864000, private")))
        .pause(1)
        .exec(http("Mobile index").get("/${instance_id}/mobile")
            .headers(headers)
            .check(status.is(200))
            .check(headerRegex("Content-Encoding", "gzip"))
            .check(headerRegex("Cache-Control", "no-cache")))
        .pause(1)
        .exec(http("Mobile config.js").get("/${instance_id}/mobile/config.js")
            .queryParam("role", "demo")
            .headers(headers)
            .check(status.is(200))
            .check(headerRegex("Content-Encoding", "gzip"))
            .check(headerRegex("Cache-Control", "max-age=864000, private")))
        .pause(1)
        .exec(http("api.js").get("/${instance_id}/wsgi/api.js")
            .headers(headers)
            .check(status.is(200))
            .check(headerRegex("Content-Encoding", "gzip"))
            .check(headerRegex("Cache-Control", "no-cache")))
        .pause(1)
        .exec(http("Xapi.js").get("/${instance_id}/wsgi/xapi.js")
            .headers(headers)
            .check(status.is(200))
            .check(headerRegex("Cache-Control", "no-cache")))
        .pause(1)
        .exec(http("Static").get("/${instance_id}/wsgi/proj/${cache_version}/build/app.js")
            .headers(headers)
            .check(status.is(200))
            .check(headerRegex("Content-Encoding", "gzip"))
            .check(headerRegex("Cache-Control", "max-age=864000"))
            .check(headerRegex("Cache-Control", "public")))
        .pause(1)

        .exec(http("Logout").get("/${instance_id}/wsgi/logout")
            .queryParam("_dc", cache_version)
            .headers(headers)
            .check(status.is(200))
            .check(headerRegex("Cache-Control", "no-cache")))

    setUp(scn.inject(rampUsers(nbUser) over (rampTime seconds))).protocols(httpProtocol)
}
