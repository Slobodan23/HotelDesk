package com.hoteldesk.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "kartice")
@Getter
@Setter
@NoArgsConstructor
public class Kartica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String imeNosioca;

    @NotBlank
    @Pattern(regexp = "\\d{4}")
    @Column(nullable = false, length = 4)
    private String poslednjeCetiriCifre;

    @Min(1)
    @Max(12)
    @Column(nullable = false)
    private int mesecIsteka;

    @Column(nullable = false)
    private int godinaIsteka;

    @JsonIgnore
    private String token;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "gost_id", nullable = false)
    private Gost gost;
}
