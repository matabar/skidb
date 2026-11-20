package com.matmon.skidb.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public final class Config {

    @JsonProperty("base-directory")
    private String baseDirectory;

    @JsonProperty("schema-file")
    private String schemaFile;

    @JsonProperty("documents-folder")
    private String documentsFolder;

    @JsonProperty("indexes-folder")
    private String indexesFolder;
}
