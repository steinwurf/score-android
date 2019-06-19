News for score-android
======================

This file lists the major changes between versions. For a more detailed list of
every change, see the Git log.

Latest
------
* Major: Upgrade to score 29
* Patch: Fix issue with ``writeToMessage`` not working properly.

4.0.1
-----
* Patch: Upgrade to score 28.0.2

4.0.0
-----
* Major: Upgrade to score 28

3.0.0
-----
* Minor: Use gradle 4.4.
* Major: Move arm and armv7 libraries into armeabi-v7a jni folder.
* Major: Moved ``MAX_SYMBOL_SIZE`` and ``MAX_GENERATION_SIZE`` from ``Source``
  into ``AutoSource`` and ``ManualSource``.

2.0.1
-----
* Patch: Fix JavaDoc errors.

2.0.0
-----
* Major: Renamed Source to ``ManualSource``.
* Major: Added new abstract base class for sources called ``Source``.
* Minor: Added ``AutoSource``.
* Major: Use androidGitVersion for setting the version of the apps and
  libraries automatically.

1.0.0
-----
* Major: Initial release.
