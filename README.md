Yotsuba Droid
=============

This is the Yotsuba Droid Android application project. This is a project to
develop a decent 4chan viewing application for Android devices, targetting Ice
Cream Sandwich and above.

This application is released under the GNU General Public Licence, version 3.
The full text of this licence can be found in the 'licence' directory.

Building the project
=====================

This project depends on the Android Utils project. The Utils project must be
referenced by this project to compile it. Help for how to do this is available
on Google's website:

http://developer.android.com/tools/projects/projects-eclipse.html

Changelog
=========

1.1
---

* Fixed the quotelink tapping, which broke with a 4chan update.
* Fixed some rare crashes caused by high latency connections.
* Switched to HTTPS for all file links.
* Switched to a.4cdn.org for API requests, which will be more future proof
  and perhaps ever so slightly faster.
* Added URI handlers so 4chan URLs can be opened with the application.
