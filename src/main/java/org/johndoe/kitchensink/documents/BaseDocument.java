package org.johndoe.kitchensink.documents;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

/**
 * BaseDocument is an abstract class that provides common fields for document creation and modification timestamps.
 */
@Data
public abstract class BaseDocument {

    /**
     * The date and time when the document was created.
     */
    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;
    /**
     * The date and time when the document was last updated.
     */
    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;

    /**
     * Default constructor for BaseDocument.
     */
    public BaseDocument() {
    }
}