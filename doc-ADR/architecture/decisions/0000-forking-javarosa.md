# 0. Forking JavaRosa

Date: 2023-06-09

## Status

Accepted

## Context

JavaRosa has been stable for many years. While functional, from a technical perspective it could use a bit of TLC. The Java has moved on, some of its libraries and tooling could use an upgrade, and it may benefit from moving from a procedural approach to a more functional style of programming.

Separately, I’ve been on the front lines of the refugee crisis in Europe and on the Mediterranean. I wanted to apply my professional skills on something that impacts humanitarian aid. There are a couple open source projects out there that are being actively developed, and contributing to one of them was my first consideration. I was hoping that I could find one using Kotlin or at least actively moving towards it. I was looking at a few java projects (ODK, OpenMRS, DHIS2). What finally pushed me in the direction of JavaRosa was this thread on ODK's [slack](https://getodk.slack.com/archives/C35ENHA64/p1683242586538609)

Reviewing JavaRosa's codebase, it was a good combination of a
- codebase with a fairly strong test-suite, precursors for getting up to speed quickly _and_ having a safety net, especially for edge cases
- it's coding style doesn't match the modern java approach, relies on deprecated code and dependencies that need paying attention-to.
- stable functionality means I could focus on my craft-of-coding skills

Finally, I am between projects (I tend to work as a contractor) and wanted a project that I could do on my own time, and pick up and put down as my situation allowed.

## Decision

Start with an area of the codebase that needs updating from a technical approach and deprecation perspective that should have no impact on functional capability of the system as a whole.

Upgrading to `java.time.*` approach to dates and time seemed to be a choice that fit all the criteria. Within that, I wanted to temporarily retain _**but isolate**_ use of `java.util.Date` to the edge where XPath Expressions are interpreted, and replace `Jodatime` with the `java.time.*` "library". A future effort could like completing the move from `java.util` to `java.time`

## Consequences

At a minimum, it's an opportunity for me to skill up and improve my craftmanship on a feature rich codebase.

At best, the ODK project team finds it useful and I make a positive contribution to society.

## About me:
My name is Jack Bolles. If you are reading this, you can already see my [github](https://github.com/jack-bolles/javarosa). My linkedin profile is [here](https://www.linkedin.com/in/jackbolles/). I was a (mostly) java dev for years, went into tech mgmt and executive roles, and decided to come back to developing in the latter stages of 2021, and have been working in Kotlin and Typescript. I'm based in London.
