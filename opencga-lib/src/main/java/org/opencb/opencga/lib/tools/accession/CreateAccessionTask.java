package org.opencb.opencga.lib.tools.accession;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.ArrayUtils;
import org.opencb.biodata.formats.variant.vcf4.VcfRecord;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.VariantAggregatedVcfFactory;
import org.opencb.biodata.models.variant.VariantSource;
import org.opencb.biodata.models.variant.VariantVcfFactory;
import org.opencb.commons.run.Task;

/**
 *
 * @author Cristina Yenyxe Gonzalez Garcia <cyenyxe@ebi.ac.uk>
 */
public class CreateAccessionTask extends Task<VcfRecord> {

    private final Character[] validCharacters = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        'B', 'C', 'D', 'F', 'G', 'H', 'J', 'K', 'L', 'M',
        'N', 'P', 'Q', 'R', 'S', 'T', 'V', 'W', 'X', 'Y', 'Z'
    };

    private VariantSource source;
    private String globalPrefix;
    private String studyPrefix;

    private LRUCache<String, Map<Variant.VariantType, String>> currentAccessions;
    private String lastAccession;

    private CombinationIterator<Character> iterator;
    private VariantVcfFactory variantFactory;

    public CreateAccessionTask(VariantSource source, String globalPrefix, String studyPrefix) {
        this(source, globalPrefix, studyPrefix, 0);
    }

    public CreateAccessionTask(VariantSource source, String globalPrefix, String studyPrefix, int priority) {
        this(source, globalPrefix, studyPrefix, null, priority);
    }

    public CreateAccessionTask(VariantSource source, String globalPrefix, String studyPrefix, String lastAccession) {
        this(source, globalPrefix, studyPrefix, lastAccession, 0);
    }
    
    public CreateAccessionTask(VariantSource source, String globalPrefix, String studyPrefix, String lastAccession, int priority) {
        super(priority);
        this.source = source;
        this.globalPrefix = globalPrefix != null ? globalPrefix : "";
        this.studyPrefix = studyPrefix;
        this.lastAccession = lastAccession;
        this.currentAccessions = new LRUCache<>(10);
        if (lastAccession != null && lastAccession.length() == 7) {
            this.iterator = new CombinationIterator(7, validCharacters, ArrayUtils.toObject(this.lastAccession.toCharArray()));
        } else {
            this.iterator = new CombinationIterator(7, validCharacters);
        }

        this.variantFactory = new VariantAggregatedVcfFactory(); // Do not even try to parse the samples, it's useless
    }

    @Override
    public boolean apply(List<VcfRecord> batch) throws IOException {
        for (VcfRecord record : batch) {
            List<Variant> variants = variantFactory.create(source, record.toString());
            StringBuilder allAccessionsInRecord = new StringBuilder();
            for (Variant v : variants) {
                Map<Variant.VariantType, String> variantAccession = currentAccessions.get(getKey(v));
                if (variantAccession != null) {
                    String typeAccession = variantAccession.get(v.getType());
                    if (typeAccession != null) {
                        allAccessionsInRecord = appendAccession(allAccessionsInRecord, typeAccession);
                    } else {
                        resetAccessions(v);
                        allAccessionsInRecord = appendAccession(allAccessionsInRecord, lastAccession);
                    }
                } else {
                    resetAccessions(v);
                    allAccessionsInRecord = appendAccession(allAccessionsInRecord, lastAccession);
                }
            }
            
            // Set accession/s for this record (be it in a new genomic position or not)
            record.addInfoField("ACC=" + allAccessionsInRecord.toString());
        }

        return true;
    }

    private String getKey(Variant v) {
        return v.getChromosome() + "_" + v.getStart();
    }
    
    private void resetAccessions(Variant v) {
        Character[] next = (Character[]) iterator.next();
        StringBuilder sb = new StringBuilder(next.length);
        for (Character c : next) {
            sb.append(c);
        }
        lastAccession = sb.toString();
        
        Map<Variant.VariantType, String> variantAccession = currentAccessions.get(getKey(v));
        if (variantAccession == null) {
            variantAccession = new HashMap<>();
            variantAccession.put(v.getType(), lastAccession);
            currentAccessions.put(getKey(v), variantAccession);
        } else {
            String typeAccession = variantAccession.get(v.getType());
            if (typeAccession == null) {
                variantAccession.put(v.getType(), lastAccession);
            }
        }
    }

    private StringBuilder appendAccession(StringBuilder allAccessionsInRecord, String newAccession) {
        if (allAccessionsInRecord.length() == 0) {
            return allAccessionsInRecord.append(globalPrefix).append(studyPrefix).append(newAccession);
        } else {
            return allAccessionsInRecord.append(",").append(globalPrefix).append(studyPrefix).append(newAccession);
        }
    }
    
}