package org.javarosa.benchmarks;

import static org.javarosa.benchmarks.BenchmarkUtils.dryRun;
import static org.javarosa.benchmarks.BenchmarkUtils.getNigeriaWardsXMLWithExternal2ndryInstance;

import java.io.IOException;
import java.nio.file.Path;
import org.javarosa.xform.parse.FormParserHelper;
import org.javarosa.xform.parse.ParseException;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

public class FormParserHelperParseExternalInstanceBenchmark {
    public static void main(String[] args) {
        dryRun(FormParserHelperParseExternalInstanceBenchmark.class);
    }

    @State(Scope.Thread)
    public static class FormParserHelperParseExternalInstanceBenchmarkState {
        Path xFormFilePath;

        @Setup(Level.Trial)
        public void initialize() {
            xFormFilePath = getNigeriaWardsXMLWithExternal2ndryInstance();
        }
    }

    @Benchmark
    public void
    benchmarkParseExternalSecondaryInstance(FormParserHelperParseExternalInstanceBenchmarkState state, Blackhole bh) throws IOException, ParseException {
        bh.consume(FormParserHelper.parse(state.xFormFilePath));
    }

}
