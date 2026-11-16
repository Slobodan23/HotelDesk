package com.hoteldesk.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "gosti")
@Getter
@Setter
@NoArgsConstructor
public class Gost extends Korisnik {

    @JsonIgnore
    @OneToMany(mappedBy = "gost")
    private List<Rezervacija> rezervacije = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "gost")
    private List<Recenzija> recenzije = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "gost", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Kartica> kartice = new ArrayList<>();
}
