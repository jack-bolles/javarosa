package org.javarosa.entities;

import kotlin.Pair;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.test.Scenario;
import org.javarosa.core.util.XFormsElement;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.entities.internal.Entities;
import org.javarosa.entities.internal.EntityFormParseProcessor;
import org.javarosa.xform.parse.ParseException;
import org.javarosa.xform.parse.XFormParserFactory;
import org.javarosa.xform.util.XFormUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.javarosa.core.util.BindBuilderXFormsElement.bind;
import static org.javarosa.core.util.XFormsElement.body;
import static org.javarosa.core.util.XFormsElement.head;
import static org.javarosa.core.util.XFormsElement.input;
import static org.javarosa.core.util.XFormsElement.item;
import static org.javarosa.core.util.XFormsElement.mainInstance;
import static org.javarosa.core.util.XFormsElement.model;
import static org.javarosa.core.util.XFormsElement.select1;
import static org.javarosa.core.util.XFormsElement.t;
import static org.javarosa.core.util.XFormsElement.title;

public class EntitiesTest {

    private final EntityXFormParserFactory entityXFormParserFactory = new EntityXFormParserFactory();

    @Before
    public void setup() {
        XFormUtils.setXFormParserFactory(entityXFormParserFactory);
    }

    @After
    public void teardown() {
        XFormUtils.setXFormParserFactory(new XFormParserFactory());
    }

    @Test
    public void fillingFormWithoutCreate_doesNotCreateAnyEntities() throws IOException, ParseException {
        Scenario scenario = Scenario.init("Entity form", XFormsElement.html(
            asList(
                new Pair<>("entities", "http://www.opendatakit.org/xforms/entities")
            ),
            head(
                title("Entity form"),
                model(asList(new Pair<>("entities:entities-version", EntityFormParseProcessor.SUPPORTED_VERSION + ".1")),
                    mainInstance(
                        t("data id=\"entity-form\"",
                            t("name"),
                            t("meta",
                                t("entity dataset=\"people\"")
                            )
                        )
                    ),
                    bind("/data/name").type("string").withAttribute("entities", "saveto", "name")
                )
            ),
            body(
                input("/data/name")
            )
        ));

        scenario.getFormEntryController().addPostProcessor(new EntityFormFinalizationProcessor());

        scenario.next();
        scenario.answer("Tom Wambsgans");

        scenario.finalizeInstance();
        List<Entity> entities = scenario.getFormEntryController().getModel().getExtras().get(Entities.class).getEntities();
        assertThat(entities.size(), equalTo(0));
    }

    @Test
    public void fillingFormWithCreate_makesEntityAvailable() throws IOException, ParseException {
        Scenario scenario = Scenario.init("Create entity form", XFormsElement.html(
            asList(
                new Pair<>("entities", "http://www.opendatakit.org/xforms/entities")
            ),
            head(
                title("Create entity form"),
                model(asList(new Pair<>("entities:entities-version", EntityFormParseProcessor.SUPPORTED_VERSION + ".1")),
                    mainInstance(
                        t("data id=\"create-entity-form\"",
                            t("name"),
                            t("meta",
                                t("entity dataset=\"people\" create=\"1\"")
                            )
                        )
                    ),
                    bind("/data/name").type("string").withAttribute("entities", "saveto", "name")
                )
            ),
            body(
                input("/data/name")
            )
        ));

        scenario.getFormEntryController().addPostProcessor(new EntityFormFinalizationProcessor());

        scenario.next();
        scenario.answer("Tom Wambsgans");

        scenario.finalizeInstance();
        List<Entity> entities = scenario.getFormEntryController().getModel().getExtras().get(Entities.class).getEntities();
        assertThat(entities.size(), equalTo(1));
        assertThat(entities.get(0).dataset, equalTo("people"));
        assertThat(entities.get(0).properties, equalTo(asList(new Pair<>("name", "Tom Wambsgans"))));
    }

    @Test
    public void fillingFormWithDynamicCreateExpression_conditionallyCreatesEntities() throws IOException, ParseException {
        Scenario scenario = Scenario.init("Create entity form", XFormsElement.html(
            asList(
                new Pair<>("entities", "http://www.opendatakit.org/xforms/entities")
            ),
            head(
                title("Create entity form"),
                model(asList(new Pair<>("entities:entities-version", EntityFormParseProcessor.SUPPORTED_VERSION + ".1")),
                    mainInstance(
                        t("data id=\"create-entity-form\"",
                            t("name"),
                            t("join"),
                            t("meta",
                                t("entity dataset=\"members\" create=\"\"")
                            )
                        )
                    ),
                    bind("/data/meta/entity/@create").calculate("/data/join = 'yes'"),
                    bind("/data/name").type("string").withAttribute("entities", "saveto", "name")
                )
            ),
            body(
                input("/data/name"),
                select1("/data/join", item("yes", "Yes"), item("no", "No"))
            )
        ));

        scenario.getFormEntryController().addPostProcessor(new EntityFormFinalizationProcessor());

        scenario.next();
        scenario.answer("Roman Roy");
        scenario.next();
        scenario.answer(scenario.choicesOf("/data/join").get(0));

        scenario.finalizeInstance();
        List<Entity> entities = scenario.getFormEntryController().getModel().getExtras().get(Entities.class).getEntities();
        assertThat(entities.size(), equalTo(1));

        scenario.newInstance();
        scenario.getFormEntryController().addPostProcessor(new EntityFormFinalizationProcessor());
        scenario.next();
        scenario.answer("Roman Roy");
        scenario.next();
        scenario.answer(scenario.choicesOf("/data/join").get(1));

        scenario.finalizeInstance();
        entities = scenario.getFormEntryController().getModel().getExtras().get(Entities.class).getEntities();
        assertThat(entities.size(), equalTo(0));
    }

    @Test
    public void entityFormCanBeSerialized() throws IOException, DeserializationException, ParseException {
        Scenario scenario = Scenario.init("Create entity form", XFormsElement.html(
            asList(
                new Pair<>("entities", "http://www.opendatakit.org/xforms/entities")
            ),
            head(
                title("Create entity form"),
                model(asList(new Pair<>("entities:entities-version", EntityFormParseProcessor.SUPPORTED_VERSION + ".1")),
                    mainInstance(
                        t("data id=\"create-entity-form\"",
                            t("name"),
                            t("meta",
                                t("entities:entity dataset=\"people\" create=\"1\"")
                            )
                        )
                    ),
                    bind("/data/name").type("string").withAttribute("entities", "saveto", "name")
                )
            ),
            body(
                input("/data/name")
            )
        ));

        scenario.getFormEntryController().addPostProcessor(new EntityFormFinalizationProcessor());

        Scenario deserializedScenario = scenario.serializeAndDeserializeForm();
        deserializedScenario.getFormEntryController().addPostProcessor(new EntityFormFinalizationProcessor());

        deserializedScenario.next();
        deserializedScenario.answer("Shiv Roy");

        deserializedScenario.finalizeInstance();
        List<Entity> entities = deserializedScenario.getFormEntryController().getModel().getExtras().get(Entities.class).getEntities();
        assertThat(entities.size(), equalTo(1));
        assertThat(entities.get(0).dataset, equalTo("people"));
        assertThat(entities.get(0).properties, equalTo(asList(new Pair<>("name", "Shiv Roy"))));
    }

    @Test
    public void entitiesNamespaceWorksRegardlessOfName() throws IOException, DeserializationException, ParseException {
        Scenario scenario = Scenario.init("Create entity form", XFormsElement.html(
            asList(
                new Pair<>("blah", "http://www.opendatakit.org/xforms/entities")
            ),
            head(
                title("Create entity form"),
                model(asList(new Pair<>("blah:entities-version", EntityFormParseProcessor.SUPPORTED_VERSION + ".1")),
                    mainInstance(
                        t("data id=\"create-entity-form\"",
                            t("name"),
                            t("meta",
                                t("entity dataset=\"people\" create=\"1\"")
                            )
                        )
                    ),
                    bind("/data/name").type("string").withAttribute("blah", "saveto", "name")
                )
            ),
            body(
                input("/data/name")
            )
        ));

        scenario.getFormEntryController().addPostProcessor(new EntityFormFinalizationProcessor());

        scenario.next();
        scenario.answer("Tom Wambsgans");

        scenario.finalizeInstance();
        List<Entity> entities = scenario.getFormEntryController().getModel().getExtras().get(Entities.class).getEntities();
        assertThat(entities.size(), equalTo(1));
        assertThat(entities.get(0).properties, equalTo(asList(new Pair<>("name", "Tom Wambsgans"))));
    }

    @Test
    public void fillingFormWithSelectSaveTo_andWithCreate_savesValuesCorrectlyToEntity() throws IOException, ParseException {
        Scenario scenario = Scenario.init("Create entity form", XFormsElement.html(
            asList(
                new Pair<>("entities", "http://www.opendatakit.org/xforms/entities")
            ),
            head(
                title("Create entity form"),
                model(asList(new Pair<>("entities:entities-version", EntityFormParseProcessor.SUPPORTED_VERSION + ".1")),
                    mainInstance(
                        t("data id=\"create-entity-form\"",
                            t("team"),
                            t("meta",
                                t("entity dataset=\"people\" create=\"1\"")
                            )
                        )
                    ),
                    bind("/data/team").type("string").withAttribute("entities", "saveto", "team")
                )
            ),
            body(
                select1("/data/team", item("kendall", "Kendall"), item("logan", "Logan"))
            )
        ));

        scenario.getFormEntryController().addPostProcessor(new EntityFormFinalizationProcessor());

        scenario.next();
        scenario.answer(scenario.choicesOf("/data/team").get(0));

        scenario.finalizeInstance();
        List<Entity> entities = scenario.getFormEntryController().getModel().getExtras().get(Entities.class).getEntities();
        assertThat(entities.size(), equalTo(1));
        assertThat(entities.get(0).properties, equalTo(asList(new Pair<>("team", "kendall"))));
    }

    @Test
    public void whenSaveToQuestionIsNotAnswered_entityPropertyIsEmptyString() throws IOException, ParseException {
        Scenario scenario = Scenario.init("Create entity form", XFormsElement.html(
            asList(
                new Pair<>("entities", "http://www.opendatakit.org/xforms/entities")
            ),
            head(
                title("Create entity form"),
                model(asList(new Pair<>("entities:entities-version", EntityFormParseProcessor.SUPPORTED_VERSION + ".1")),
                    mainInstance(
                        t("data id=\"create-entity-form\"",
                            t("name"),
                            t("meta",
                                t("entity dataset=\"people\" create=\"1\"")
                            )
                        )
                    ),
                    bind("/data/name").type("string").withAttribute("entities", "saveto", "name")
                )
            ),
            body(
                input("/data/name")
            )
        ));

        scenario.getFormEntryController().addPostProcessor(new EntityFormFinalizationProcessor());
        scenario.finalizeInstance();

        List<Entity> entities = scenario.getFormEntryController().getModel().getExtras().get(Entities.class).getEntities();
        assertThat(entities.size(), equalTo(1));
        assertThat(entities.get(0).properties, equalTo(asList(new Pair<>("name", ""))));
    }

    @Test
    public void savetoIsRemovedFromBindAttributesForClients() throws IOException, ParseException {
        Scenario scenario = Scenario.init("Create entity form", XFormsElement.html(
            asList(
                new Pair<>("entities", "http://www.opendatakit.org/xforms/entities")
            ),
            head(
                title("Create entity form"),
                model(asList(new Pair<>("entities:entities-version", EntityFormParseProcessor.SUPPORTED_VERSION + ".1")),
                    mainInstance(
                        t("data id=\"create-entity-form\"",
                            t("name"),
                            t("meta",
                                t("entity dataset=\"people\" create=\"1\"")
                            )
                        )
                    ),
                    bind("/data/name").type("string").withAttribute("entities", "saveto", "name")
                )
            ),
            body(
                input("/data/name")
            )
        ));

        scenario.next();
        List<TreeElement> bindAttributes = scenario.getFormEntryPromptAtIndex().getBindAttributes();
        boolean containsSaveTo = bindAttributes.stream().anyMatch(treeElement -> treeElement.getName().equals("saveto"));
        assertThat(containsSaveTo, is(false));
    }
}
