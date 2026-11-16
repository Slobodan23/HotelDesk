package com.hoteldesk.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vlasnici")
@Getter
@Setter
@NoArgsConstructor
public class Vlasnik extends Korisnik {

    @JsonIgnore
    @OneToMany(mappedBy = "vlasnik")
    private List<Hotel> hoteli = new ArrayList<>();
}
