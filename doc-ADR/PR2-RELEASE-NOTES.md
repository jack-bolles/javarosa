# Release Notes:

## PR 2 [link When Pushed]

+ `TimeData` uses the `java.time.LocalTime` as its underlying data structure. This solves the problem identified by these tests : [TimeDataLimitationsTest](https://github.com/getodk/javarosa/blob/6d077f57e53c26c862e666b22325b090e1190895/src/test/java/org/javarosa/core/model/data/test/TimeDataLimitationsTest.java)

+ Moved Period math from DateUtils to IPreLoader, where's its used.
+ 
+ *Ongoing - Upgraded parts of the codebase to take advantage of `Java8` features* - done largely taking advantage of Intellij's code analysis and low-hanging fruit as I was working on the Date migration.

+ `build.gradle` no longer uses syntax deprecated for v8.+ 


### More work to do 
+ Convert `DateTimeData` to use `java.time.*`

## Hangover from previous
### More work to do
+ There are unknown set of formats potentially allowed (see `org.javarosa.core.model.utils.DateFormatter.xpathPatternAsJavaTimePattern()` ). These should(?) be encapsulated in a `DateFormat` of its own. It's tested against the formats used in the codebase, included classes testing other functionality, but thorough testing of the xpath patterns still needs to be done.

+ The hard coded patterns used in `DateFormat` could/should be replaced where applicable by constants in `DateTimeFormatter`.

### Unknowns:
The code uses an int to look up in `org.javarosa.core.model.utils.DateFormatter` which pattern to use when formatting. The numbers are 1,2, 7, and 9 (and 5, which was removed as per above). What happened to the other numbers and how are. they either restricted from use or meant to be processed? Understanding this would allow direct reference to the `DateFormat` enums without doing a lookup.