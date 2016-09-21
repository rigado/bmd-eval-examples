package com.rigado.bmd200eval.utilities;

/**
 * Used to read the JSON records from res/raw
 * The member variable names must match the key names in the JSON
 */
public class JsonFirmwareType {

    // These two fields are always expected to exist in the JSON
    private String fwname;
    private Properties properties;

    public String getFwname() {
        return fwname;
    }
    public void setFwname(String fwname) {
        this.fwname = fwname;
    }
    public Properties getProperties() {
        return properties;
    }
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    // sub-structure in JSON, any of these fields may be omitted and an empty string will be returned by getter instead of null
    public class Properties {
        private String version;
        private String build;
        private String filename_200;
        private String filename_300;
        private String comment;

        public String getVersion() {
            if (version == null) return "";
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getBuild() {
            if (build == null) return "";
            return build;
        }
        public void setBuild(String build) {
            this.build = build;
        }

        public String getFilename200() {
            if (filename_200 == null) return "";
            return filename_200;
        }

        public void setFilename200(String filename) {
            this.filename_200 = filename;
        }

        public String getFilename300() {
            if(filename_300 == null) return "";
            return filename_300;
        }

        public void setFilename300(String filename) { this.filename_300 = filename; }

        public String getComment() {
            if (comment == null) return "";
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }
    }
}
