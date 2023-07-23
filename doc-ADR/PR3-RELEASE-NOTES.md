# Release Notes:

## PR 3 [link When Pushed]

+ Code Cleanup. Each commit is the application of one or a few (bundled when impact was small) applications of fixes suggested by Intelli's `Inspect Code` tool

### More work to do 
+ There is much de-duplication and removing references to deprecated code, and removing the deprecated code itself.

## Hangover from previous
### More work to do
+ Convert `DateTimeData` to use `java.time.*`

+ There are unknown set of formats potentially allowed (see `org.javarosa.core.model.utils.DateFormatter.xpathPatternAsJavaTimePattern()` ). These should(?) be encapsulated in a `DateFormat` of its own. It's tested against the formats used in the codebase, included classes testing other functionality, but thorough testing of the xpath patterns still needs to be done.

+ The hard coded patterns used in `DateFormat` could/should be replaced where applicable by constants in `DateTimeFormatter`.

### Unknowns:
The code uses an int to look up in `org.javarosa.core.model.utils.DateFormatter` which pattern to use when formatting. The numbers are 1,2, 7, and 9 (and 5, which was removed as per above). What happened to the other numbers and how are. they either restricted from use or meant to be processed? Understanding this would allow direct reference to the `DateFormat` enums without doing a lookup.