#! /usr/bin/env python
# encoding: utf-8

import os

APPNAME = 'score-android'
VERSION = '5.0.1'


def configure(conf):

    conf.check_cxx(lib='android')


def build(bld):

    bld.env.append_unique(
        'DEFINES_STEINWURF_VERSION',
        'STEINWURF_SCORE_ANDROID_VERSION="{}"'.format(VERSION))

    bld.recurse('jni')

    if bld.is_toplevel():

        # Install the APK files from "app/gradle_build/outputs/apk/debug"
        if bld.has_tool_option('install_path'):

            install_path = bld.get_tool_option('install_path')
            install_path = os.path.abspath(os.path.expanduser(install_path))
            relative_dir = bld.bldnode.path_from(bld.srcnode)

            apps = ['app']
            for app in apps:
                start_dir = bld.path.find_dir(
                    '{}/gradle_build/outputs/apk/debug'.format(app))
                bld.install_files(os.path.join(install_path, relative_dir),
                                  start_dir.ant_glob('*.apk'),
                                  cwd=start_dir)
