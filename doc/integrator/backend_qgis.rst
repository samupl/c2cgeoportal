.. _integrator_backend_qgis:

=========================================
Specific configuration for QGIS mapserver
=========================================

Apache configuration
====================

In the ``apache/mapserv.conf.mako`` do the following changes:

.. code::

   - ScriptAlias /mapserv /usr/lib/cgi-bin/mapserv
   + ScriptAlias /mapserv /usr/lib/cgi-bin/qgis_mapserv.fcgi

   - SetEnv MS_MAPFILE ${directory}/mapserver/mapserver.map
   + SetEnv QGIS_PROJECT_FILE ${directory}/qgisserver.qgs

Cleanup
=======

Remove the MapServer folder:

.. prompt:: bash

   git rm mapserver

QGIS Desktop configuration
==========================

OWS configuration
*****************

You should setup your OWS service in the QGIS project properties in the OWS tab.

You should **check** *Service Capabilities* and **fill** *Online resource* to correctly generate
the URL's in the capabilities.

In the *WMS capabilities* section, you should take care to **uncheck** the checkbox *User layer ids as
names*. If this checkbox is enabled you will have unreliable layers name.

In the *WMS capabilities* section, you should **check** the checkbox *Add geometry to feature response* in
order to make the WMS GetFeatureInfo working correctly.

In the *WFS capabilities* section, you should check the layers that need to be available in the WFS service.
Also take care on the Update Insert delete column to make the layer available throw the WFS transactional
API.

Finally, your QGIS project layers CRS should all be in the same CSR. This is subject to change.

Connect to Postgres database
****************************

This section is subject to change when the QGIS plugin is available.

The way you should connect QGIS to the database is based on an external file
called ``pg_service.conf`` located in the home directory. The content of this file is as follows:

.. code::

    [geomapfish]
    host=localhost
    dbname=geomapfish
    user=www-data
    password=www-data
    port=5433

You probably need to create a tunnel with ssh:

.. prompt:: bash

   ssh -L 5432:localhost:54532 <server>

Note that if you can connect directly to the database, you don't need this tunnel.
Ask to your database administrator the correct parameters. You probably just need
to change the host parameter.

You can have several sections. A section start with a name with [] and
finish with a blank line. This file should be a unix file.

On QGIS desktop, when creating a new PostGIS connection, give it a name and use
the service name (``geomapfish`` in our example) in the connection parameters form.

Copy-past this file in the server, change the parameters to fit with the server
settings and add the variable environment setting in the Apache config:

.. code::

    [..]
    SetEnv QGIS_PROJECT_FILE ${directory}/qgisserver.qgs
    + SetEnv PGSERVICEFILE path/to/pg_service.conf

Don't forget to restart Apache.

Run the client with Docker
**************************

Create a tunnel to the database:

.. prompt:: bash

   IP=$(ip addr show dev $(route | grep default | awk '{print $(NF)}' | head -1) | awk '$1 ~ /^inet/ { sub("/.*", "", $2); print $2 }' | head -1)
   ssh -L ${IP}:5432:localhost:5432 <remote server>

In a Docker mode, QGIS is configured in the ``qgisserver/`` directory. To edit the configuration,
run this target and open the ``/etc/project/project.qgs`` file:

.. prompt:: bash

   make edit-qgis-project

If you want to add PostGIS layers on the main DB, create/edit your ``$HOME/.pg_service.conf`` file
and add a section for the DB you want to access:

.. code::

   [geomapfish]
   host=<host>  # Localhost addresse of the Docker network interface (=> ${IP})
   dbname=<database name>
   user=<database user>
   password=<database password>
   port=5432

Then, in QGIS, fill only the ``name`` and ``service`` fields when you create the DB connection.
Then, you can create/edit a ``qgisserver/pg_service.conf.tmpl`` file and add a section looking
like that:

.. code::

   [geomapfish]
   host=${PGHOST}
   dbname=${PGDATABASE}
   user=${PGUSER}
   password=${PGPASSWORD}
   port=${PGPORT}

OGC server
==========

In the project file you should set the online resource URL
(Project/Properties.../QGIS Server/General information/Online resource) to
``https://<host>/<entrypoint>/mapservproxy?ogcserver=<name>``, e.-g.
``https://geomapfish-demo-ci.camptocamp.com/mapservproxy?ogcserver=QGIS%20server``.

To use the QGIS server in authenticated mode through the mapserv proxy, it's required to be in docker mode,
and he should be configured as follow:

* Name: ``<name>``, e.-g. ``QGIS server``
* Base URL: ``http://qgisserver:8080/``
* WFS URL: empty
* Server type: ``qgisserver``
* Image type: recommended to be ``image/png``
* Authentication type: ``Standard auth``
* WFS support: recommended to be ``[X]``
* Is single tile:  recommended to be ``[ ]``

Access Restriction
******************

The access restriction is available only for Docker projects

We provide an Docker image named ``camptocamp/geomapfish-qgisserver`` with tag pattern:
``gmf<Major GeoMapFish version}-qgis${Major QGIS}``.

Configuration for a single project, just provide the OGC server name in the environment variable named:
``GEOMAPFISH_OGCSERVER``.

If you need to provide more than one QGIS projects you should write a config file named, e.g.
``qgisserver/accesscontrol_config.yaml``, with the content:

.. code:: yaml

   map_config:
     <project file path>:
       ogc_server: <OGC server name>

``<project file path>`` should have exactly the same value a the ``MAP`` parameter in the ``Base URL``
vavalue of the OGC sever.

And finally you should provide the ``GEOMAPFISH_ACCESSCONTROL_CONFIG`` to point to config file e.-g.
``/etc/qgisserver/accesscontrol_config.yaml``, and ``QGIS_PROJECT_FILE`` to be empty.
