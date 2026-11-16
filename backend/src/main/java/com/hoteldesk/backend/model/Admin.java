package com.hoteldesk.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "admini")
@NoArgsConstructor
public class Admin extends Korisnik {
}
