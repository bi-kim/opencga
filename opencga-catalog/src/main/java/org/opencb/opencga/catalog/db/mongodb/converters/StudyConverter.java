package org.opencb.opencga.catalog.db.mongodb.converters;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.bson.Document;
import org.opencb.opencga.catalog.db.CatalogDBAdaptorFactory;
import org.opencb.opencga.catalog.db.api.CatalogDBAdaptor;
import org.opencb.opencga.catalog.db.mongodb.CatalogMongoDBAdaptor;
import org.opencb.opencga.catalog.models.File;
import org.opencb.opencga.catalog.models.Study;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by pfurio on 18/01/16.
 */
public class StudyConverter extends GenericConverter<Study, Document> {

    public StudyConverter() {
        objectReader = objectMapper.reader(Study.class);
    }

    @Override
    public Study convertToDataModelType(Document object) {
        Study study = null;
        try {
            study = objectReader.readValue(objectWriter.writeValueAsString(object));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return study;
    }

    @Override
    public Document convertToStorageType(Study object) {
        Document document = null;
        try {
            document = Document.parse(objectWriter.writeValueAsString(object));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return document;
    }
}
