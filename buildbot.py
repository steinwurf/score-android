﻿#!/usr/bin/env python
# encoding: utf-8

import sys
import json
import subprocess

project_name = 'score-android'


def run_command(args, shell=False):
    print("Running: {}".format(args))
    sys.stdout.flush()
    subprocess.check_call(args, shell=shell)


def get_tool_options(properties):
    options = []
    if 'tool_options' in properties:
        # Make sure that the values are correctly comma separated
        for key, value in properties['tool_options'].items():
            if value is None:
                options += ['--{0}'.format(key)]
            else:
                options += ['--{0}={1}'.format(key, value)]

    return options


def get_compile_sdk_version():
    # We extract compileSdkVersion from build.gradle
    with open('build.gradle') as f:
        for line in f:
            # The version assigments follow this format: "name = value"
            tokens = line.strip().split(' ')
            if tokens[0] == 'compileSdkVersion':
                return tokens[2].strip("'\"")
    return None


def configure(properties):
    command = [sys.executable, 'waf']

    if properties.get('build_distclean'):
        command += ['distclean']

    # Make sure that gradle starts from a clean state
    if properties.get('build_distclean'):
        run_command(['./gradlew', 'clean'])

    command += ['configure', '--git_protocol=git@']

    if 'waf_resolve_path' in properties:
        command += ['--resolve_path=' + properties['waf_resolve_path']]

    if 'dependency_project' in properties:
        command += ['--{0}_checkout={1}'.format(
            properties['dependency_project'],
            properties['dependency_checkout'])]

    command += ["--cxx_mkspec={}".format(properties['cxx_mkspec'])]
    command += get_tool_options(properties)

    run_command(command)

    # The required sdk versions are extracted from build.gradle
    sdk_version = get_compile_sdk_version()
    if sdk_version is None:
        raise Exception('Unable to find compile sdk version')

    # Install the required Android compileSdkVersion
    command = 'echo y | $ANDROID_HOME/tools/android update sdk --all ' \
              '--filter android-{} --no-ui'.format(sdk_version)
    run_command(command, shell=True)


def build(properties):
    run_command([sys.executable, 'waf', 'build', '-v'])

    # Gradle builds the APK (this should be run after the waf build)
    run_command(['./gradlew', 'assembleDebug', '--debug'])
    run_command(['./gradlew', 'androidJavadocs'])


def run_tests(properties):
    command = [sys.executable, 'waf', '-v', '--run_tests']
    command += get_tool_options(properties)
    run_command(command)

    device_id = properties['tool_options']['device_id']

    def run_gradle_command(command):
        cmd = "ANDROID_SERIAL={} ./gradlew {}".format(device_id, command)
        run_command(cmd, shell=True)

    # Remove any previously installed versions of the app from the device
    run_gradle_command('uninstallAll')
    # Gradle installs the APK on the target device
    run_gradle_command('installDebug')
    # Gradle runs unit test on target device
    run_gradle_command('connectedAndroidTest')


def install(properties):
    command = [sys.executable, 'waf', '-v', 'install']

    if 'install_path' in properties:
        command += ['--install_path={0}'.format(properties['install_path'])]
    if properties.get('install_relative'):
        command += ['--install_relative']

    run_command(command)


def main():
    argv = sys.argv

    if len(argv) != 3:
        print("Usage: {} <command> <properties>".format(argv[0]))
        sys.exit(0)

    cmd = argv[1]
    properties = json.loads(argv[2])

    if cmd == 'configure':
        configure(properties)
    elif cmd == 'build':
        build(properties)
    elif cmd == 'run_tests':
        run_tests(properties)
    elif cmd == 'install':
        install(properties)
    else:
        print("Unknown command: {}".format(cmd))


if __name__ == '__main__':
    main()
