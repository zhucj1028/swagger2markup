package io.github.swagger2markup.internal.component;

import io.github.swagger2markup.OpenAPI2MarkupConverter;
import io.github.swagger2markup.adoc.ast.impl.TableImpl;
import io.github.swagger2markup.extension.MarkupComponent;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.parameters.RequestBody;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.ast.Table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import static io.github.swagger2markup.config.OpenAPILabels.*;
import static io.github.swagger2markup.internal.helper.OpenApiHelpers.generateInnerDoc;
import static io.github.swagger2markup.internal.helper.OpenApiHelpers.getSchemaTypeAsString;

public class RequestBodyComponent extends MarkupComponent<StructuralNode, RequestBodyComponent.Parameters, StructuralNode> {

    private final SchemaComponent schemaComponent;
    private final MediaContentComponent mediaContentComponent;
    private final MediaTypeExampleComponent mediaTypeExampleComponent;
    private final ExamplesComponent examplesComponent;


    public RequestBodyComponent(OpenAPI2MarkupConverter.OpenAPIContext context) {
        super(context);
        this.schemaComponent = new SchemaComponent(context);
        this.mediaContentComponent = new MediaContentComponent(context);
        this.mediaTypeExampleComponent = new MediaTypeExampleComponent(context);
        this.examplesComponent = new ExamplesComponent(context);
    }

    public static Parameters parameters(RequestBody body) {
        return new Parameters(body);
    }

    public StructuralNode apply(StructuralNode serverSection, RequestBody body) {
        return apply(serverSection, parameters(body));
    }

    @Override
    public StructuralNode apply(StructuralNode structuralNode, Parameters parameters) {
        RequestBody body = parameters.body;

        if (null == body ) return structuralNode;

        TableImpl pathResponsesTable = new TableImpl(structuralNode, new HashMap<>(), new ArrayList<>());
        pathResponsesTable.setOption("header");
        pathResponsesTable.setAttribute("caption", "", true);
        pathResponsesTable.setAttribute("cols", ".^2a,.^14a,.^4a", true);
        pathResponsesTable.setTitle("RequestBody");
        pathResponsesTable.setHeaderRow(
                labels.getLabel(TABLE_HEADER_TYPE),
                labels.getLabel(TABLE_HEADER_DESCRIPTION),
                labels.getLabel(TABLE_HEADER_SCHEMA));

        body.getContent().forEach((name,schema) ->{
            pathResponsesTable.addRow(
                    generateInnerDoc(pathResponsesTable, "body"),
                    getRequestBodyDescriptionColumnDocument(pathResponsesTable, schema, body.getDescription()),
                    generateInnerDoc(pathResponsesTable, getSchemaTypeAsString(schema.getSchema()))
            );
        });
        structuralNode.append(pathResponsesTable);
        return structuralNode;
    }

    private Document getRequestBodyDescriptionColumnDocument(Table table, MediaType schema, String desc) {
        Document document = generateInnerDoc(table, Optional.ofNullable(desc).orElse(""));
        mediaTypeExampleComponent.apply(document, schema.getExample());
        examplesComponent.apply(document, schema.getExamples());
        return document;
    }

    public static class Parameters {
        private final RequestBody body;
        public Parameters(RequestBody body) {
            this.body = body;
        }
    }

}
