package com.manufacturing.manufacturingindex.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class OnePagerSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Factory factory;

    @Column(length = 2000)
    private String highlight;

    @Column(length = 1000)
    private String keyAction;

    @Column(length = 1000)
    private String helpNeeded;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Factory getFactory() {
		return factory;
	}

	public void setFactory(Factory factory) {
		this.factory = factory;
	}

	public String getHighlight() {
		return highlight;
	}

	public void setHighlight(String highlight) {
		this.highlight = highlight;
	}

	public String getKeyAction() {
		return keyAction;
	}

	public void setKeyAction(String keyAction) {
		this.keyAction = keyAction;
	}

	public String getHelpNeeded() {
		return helpNeeded;
	}

	public void setHelpNeeded(String helpNeeded) {
		this.helpNeeded = helpNeeded;
	}

    
}
