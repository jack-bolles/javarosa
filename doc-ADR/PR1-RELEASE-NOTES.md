# Release Notes:

## PR 1 [https://github.com/getodk/javarosa/pull/718]

### Major changes in this version:
+ *Replaced joda-time functionality with `java.time`*
Technically complete, as the codebase no long e refers to JodaTime in any of the  code and it has been removed as a dependency in gradle+maven. The XPath Xpression parser still uses `java.util.Date` and tests refer to `java.util.TimeZone`, so the upgrade to fully using `java.time.*` still has work to do. The ultimate call to internally use `java.time.*` is a decision with potentially broad impact and will be taken later.

+ *Upgraded the codebase to take advantage of `Java8` features* - done largely taking advantage of Intellij's code analysis and low-hanging fruit as I was working on the Date migration.

+ *(Slightly) more modular structure* - known and supported data formats are encapsulated and use `DateFormatters` for, well, formatting dates, replacing the if-else tree of interpreting character by character and padding the response.

+ `AnswerData` updated to use `java.time.LocalDate` and `java.time.LocalTime` for DateData and TimeDate. DateTimeData still uses `java.util.Date`

+ 

### Breaking change -
A format referred to as `FORMAT_HUMAN_READABLE_DAYS_FROM_TODAY` in DateFormatter seemed to be better suited as a Duration. It could be re-introduced then. See [ADR 3](architecture/decisions/0003-limit-date-timeformats.md)

### More work to do 
+ Use ZonedDate or OffsetDate instead of java.util.Date for DateTimeData
+ There are unknown set of formats potentially allowed (see `org.javarosa.core.model.utils.DateFormatter.xpathPatternAsJavaTimePattern()` ). These should(?) be encapsulated in a `DateFormat` of its own. It's tested against the formats used in the codebase, included classes testing other functionality, but thorough testing of the xpath patterns still needs to be done.

+ The hard coded patterns used in `DateFormat` could/should be replaced where applicable by constants in `DateTimeFormatter`.

### Unknowns:
The code uses an int to look up in `org.javarosa.core.model.utils.DateFormatter` which pattern to use when formatting. The numbers are 1,2, 7, and 9 (and 5, which was removed as per above). What happened to the other numbers and how are. they either restricted from use or meant to be processed? Understanding this would allow direct reference to the `DateFormat` enums without doing a lookup.