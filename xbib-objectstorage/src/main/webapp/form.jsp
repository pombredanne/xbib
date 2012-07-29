<html> 
<head>
            <meta http-equiv="content-type" content="text/html; charset=UTF-8"></meta>
            <title>Objektspeicher</title>
            <style media="all" type="text/css"><![CDATA[
* { margin: 0; padding: 0; }
html { height:100%; overflow:-moz-scrollbars-vertical; overflow-x:auto }
body { font-family: "Helvetica Neue", "Helvetica", "Lucida Grande", "Arial", "sans-serif"; }
img { border:0 }
dl { width:100%; overflow:hidden; }
dt { float:left; width:10%; font-size: 320%; border-top: 4px solid black; }
dd { float:left; width:90%; border-top: 4px solid black; }
h2 { margin-bottom: 1em; }
table { border:0; border-collapse:collapse; border-spacing:0 }
fieldset { border: 0px; }
fieldset ol { margin: 0px; padding: 0px; }
fieldset li { list-style: none; margin: 0px; }
label { width: 24em; padding-right: .5em; padding-left: 1em }
input.inputline, textarea { border: 1px solid grey; width: 24em; font-size: large; margin: .5em }
#container { width: 940px; margin: -100px auto; padding: 110px 10px 50px 10px; text-align:center }
#footer { margin-top: 2em; font-size: small; }
]]></style>
        </head>
        <body>
          <div id="container">
            <div id="main">
                <form enctype="multipart/form-data"
                      action="${pageContext.request.contextPath}/api/v1/default/demo/NEW" 
                      method="POST">
                    <fieldset>
                        <div class="uploadbox">
                            Bitte PDF-Datei auswählen
                        <input type="file" id="file" name="files[]"/>
                        <input value="Hochladen" name="uploadbutton" type="submit"/>
                        </div>
                    </fieldset>
                </form>
            </div>
          </div>
        </body>
</html>