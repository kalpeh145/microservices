package com.codedecode.microservices.VaccinationCenter.Entity;

import javax.persistence.*;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Data;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VaccinationCenter {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	@Column(name = "centerName")
	private String centerName;

	@Column(name = "centerAddress")
	private String centerAddress;
	

}
