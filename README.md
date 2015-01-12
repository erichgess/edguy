# edguy

Build Status:

https://travis-ci.org/erichgess/edguy.svg?branch=master

## Prerequisites

You will need [Leiningen][] 2.0.0 or above installed.

[leiningen]: https://github.com/technomancy/leiningen

## Running

To start a web server for the application, run:

    lein ring server

## Slack grammar
The Slack trigger is `edguy`.

The Slack grammar follows this pattern

    edguy <function name> [function parameters]

Each function may have different parameters.

Here are the following defined functions:

    edguy get pull requests from me

`me` is a keyword which edguy will replace with the command sender's username.

    edguy set my GitHub to <git hub username>

This command will map your Slack user name to the given GitHub user name and vice versa.

    edguy set my Wrike to <wrike username>

This command will map your Slack user name to the given Wrike user name.

    edguy get pull request from <slack username>

This will get the pull requests opened by the given username.  This command requires that that user have their Slack account mapped to a GitHub account.

    edguy create task for <slack user name>: <Task Title>
    edguy get tasks for me
    edguy get tasks for <slack username>


The general form:
    edguy [verb] [object] {for/from/to} [subject]

    verb - The verb says what edguy should do with the given object.  Get information, update information, create something, etc.
    object - This tells edguy what data to work with.
    subject - This tells edguy which user's data it should be looking at.  For example: edguy get pull requests from @joe tells edguy to get the pull requests opened by @joe.
    {for/from/to} - The preposition serves two purposes:  first to make the commands for edguy proper English and second as a sign post to mark the end of the [object].

Edguy keywords can be multiple words.  For example: "pull requests", "my Wrike", "my GitHub".

## License

Copyright © 2014 FIXME
