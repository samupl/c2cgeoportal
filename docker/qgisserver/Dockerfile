FROM camptocamp/qgis-server:latest
LABEL maintainer Camptocamp "info@camptocamp.com"

COPY requirements.txt /tmp/
RUN python3 -m pip install --requirement /tmp/requirements.txt

COPY geomapfish_plugin/* /var/www/plugins/geomapfish_qgisserver/
COPY commons /opt/c2cgeoportal_commons

RUN python3 -m pip install --editable /opt/c2cgeoportal_commons
