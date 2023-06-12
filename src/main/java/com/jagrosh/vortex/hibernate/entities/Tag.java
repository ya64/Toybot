package com.jagrosh.vortex.hibernate.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A persistant class representing a tag
 */
@Entity
@Table(name = "TAGS")
@Data
@NoArgsConstructor
public class Tag {
    /**
     * The guild ID
     */
    @Id
    @Column(name = "GUILD_ID")
    private long guildId;

    /**
     * The name of the tag, always lowercase
     */
    @Id
    @Column(name = "NAME")
    private String name;

    /**
     * The value of the tag
     */
    @Column(name = "VALUE")
    private String value;
}