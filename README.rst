score-android
=============

.. image:: https://img.shields.io/badge/API-14%2B-brightgreen.svg?style=flat
    :target: https://android-arsenal.com/api?level=14

.. image:: http://buildbot.steinwurf.dk/svgstatus?project=score-android
    :target: http://buildbot.steinwurf.dk/stats?projects=score-android


**Score** is a library which implements the
**S**\ imple **Co**\ ded **Re**\ liable protocol for reliable unicast and
multicast communication over UDP.

This repository contains Java bindings so that Score can be used natively in Android Apps.

Requirements
------------
API Level 14 (Android 4.0) and above.

Setup
-----
The library is deployed at Steinwurf's private maven repository.

You need to be an authorized user to accessing this.

Please contact Steinwurf at support@steinwurf.com to have us make you a user.

Further more the following repository needs to be added to your project's ``build.gradle`` file:

.. code-block:: groovy

    allprojects {
        repositories {
            ...
            maven {
                url "http://artifactory.steinwurf.com/artifactory/private-libs-release-local"
                credentials {
                    username = "${artifactory_username}"
                    password = "${artifactory_password}"
                }
            }
            ...
        }
    }

``"${artifactory_username}"`` and ``"${artifactory_password}"`` are strings
defined from the variables ``artifactory_username`` and
``artifactory_password``. These values should not be committed to the
repository, but rather be private to each developer.
You can keep them private by storing them in your global your global gradle file
(This is located in ``$HOME/.gradle/gradle.properties``):

.. code-block:: groovy

    artifactory_username=[YOUR USERNAME HERE]
    artifactory_password=[YOUR PASSWORD HERE]

For security reasons it's recommended to use an encrypted password.
You can get that on your users page on our Artifactory web interface.

http://artifactory.steinwurf.com/artifactory/webapp/#/home

Once you have all this, adding the modules of this project to your module's dependency is as
simple as this:

.. code-block:: groovy

    dependencies
    {
        ...
        implementation 'com.steinwurf.score:sink:[INSERT_VERSION_NUMER]'
        implementation 'com.steinwurf.score:source:[INSERT_VERSION_NUMER]'
        ...
    }

remember to replace ``[INSERT_VERSION_NUMER]`` with the most recent version
number.

Build
-----
Coming Soon...

Usage
-----
Coming Soon...

License
-------

THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF STEINWURF.
