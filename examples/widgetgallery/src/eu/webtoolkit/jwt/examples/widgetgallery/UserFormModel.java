/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.widgetgallery;

import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.servlet.*;
import eu.webtoolkit.jwt.utils.*;
import java.io.*;
import java.lang.ref.*;
import java.time.*;
import java.util.*;
import java.util.regex.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class UserFormModel extends WFormModel {
  private static Logger logger = LoggerFactory.getLogger(UserFormModel.class);

  public static final String FirstNameField = "first-name";
  public static final String LastNameField = "last-name";
  public static final String CountryField = "country";
  public static final String CityField = "city";
  public static final String BirthField = "birth";
  public static final String ChildrenField = "children";
  public static final String RemarksField = "remarks";

  public UserFormModel() {
    super();
    this.countryModel_ = null;
    this.cityModel_ = null;
    this.initializeModels();
    this.addField(FirstNameField);
    this.addField(LastNameField);
    this.addField(CountryField);
    this.addField(CityField);
    this.addField(BirthField);
    this.addField(ChildrenField);
    this.addField(RemarksField);
    this.setValidator(FirstNameField, this.createNameValidator(FirstNameField));
    this.setValidator(LastNameField, this.createNameValidator(LastNameField));
    this.setValidator(CountryField, this.createCountryValidator());
    this.setValidator(CityField, this.createCityValidator());
    this.setValidator(BirthField, this.createBirthValidator());
    this.setValidator(ChildrenField, this.createChildrenValidator());
    this.setValue(BirthField, null);
    this.setValue(CountryField, "");
  }

  public WAbstractItemModel getCountryModel() {
    return this.countryModel_;
  }

  public int countryModelRow(final String code) {
    for (int i = 0; i < this.countryModel_.getRowCount(); ++i) {
      if (this.countryCode(i).equals(code)) {
        return i;
      }
    }
    return -1;
  }

  public WAbstractItemModel getCityModel() {
    return this.cityModel_;
  }

  public void updateCityModel(final String countryCode) {
    this.cityModel_.clear();
    List<String> i = cities.get(countryCode);
    if (i != null) {
      final List<String> cities = i;
      this.cityModel_.appendRow(new WStandardItem());
      for (int j = 0; j < cities.size(); ++j) {
        this.cityModel_.appendRow(new WStandardItem(cities.get(j)));
      }
    } else {
      this.cityModel_.appendRow(new WStandardItem("(Choose Country first)"));
    }
  }

  public WString getUserData() {
    return StringUtils.asString(this.getValue(FirstNameField))
        .append(" ")
        .append(StringUtils.asString(this.getValue(LastNameField)))
        .append(": country code=")
        .append(StringUtils.asString(this.getValue(CountryField)))
        .append(", city=")
        .append(StringUtils.asString(this.getValue(CityField)))
        .append(", birth=")
        .append(StringUtils.asString(this.getValue(BirthField)))
        .append(", children=")
        .append(StringUtils.asString(this.getValue(ChildrenField)))
        .append(", remarks=")
        .append(StringUtils.asString(this.getValue(RemarksField)))
        .append(".");
  }

  public String countryCode(int row) {
    return StringUtils.asString(this.countryModel_.getData(row, 0, ItemDataRole.User)).toString();
  }

  private static final Map<String, List<String>> cities = getCityMap();
  private static final Map<String, String> countries = getCountryMap();
  private WStandardItemModel countryModel_;
  private WStandardItemModel cityModel_;
  private static final int MAX_LENGTH = 25;
  private static final int MAX_CHILDREN = 15;

  private void initializeModels() {
    int countryModelRows = countries.size() + 1;
    final int countryModelColumns = 1;
    this.countryModel_ = new WStandardItemModel(countryModelRows, countryModelColumns);
    int row = 0;
    this.countryModel_.setData(row, 0, " ", ItemDataRole.Display);
    this.countryModel_.setData(row, 0, "", ItemDataRole.User);
    row = 1;
    for (Iterator<Map.Entry<String, String>> i_it = countries.entrySet().iterator();
        i_it.hasNext(); ) {
      Map.Entry<String, String> i = i_it.next();
      this.countryModel_.setData(row, 0, i.getValue(), ItemDataRole.Display);
      this.countryModel_.setData(row++, 0, i.getKey(), ItemDataRole.User);
    }
    this.cityModel_ = new WStandardItemModel();
    this.updateCityModel("");
  }

  private WValidator createNameValidator(final String field) {
    WLengthValidator v = new WLengthValidator();
    v.setMandatory(true);
    v.setMinimumLength(1);
    v.setMaximumLength(MAX_LENGTH);
    return v;
  }

  private WValidator createCountryValidator() {
    WLengthValidator v = new WLengthValidator();
    v.setMandatory(true);
    return v;
  }

  private WValidator createCityValidator() {
    WLengthValidator v = new WLengthValidator();
    v.setMandatory(true);
    return v;
  }

  private WValidator createBirthValidator() {
    WDateValidator v = new WDateValidator();
    v.setBottom(new WDate(1900, 1, 1));
    v.setTop(WDate.getCurrentDate());
    v.setFormat("dd/MM/yyyy");
    v.setMandatory(true);
    return v;
  }

  private WValidator createChildrenValidator() {
    WIntValidator v = new WIntValidator(0, MAX_CHILDREN);
    v.setMandatory(true);
    return v;
  }

  static Map<String, String> getCountryMap() {
    Map<String, String> retval = new HashMap<String, String>();
    retval.put("BE", "Belgium");
    retval.put("NL", "Netherlands");
    retval.put("UK", "United Kingdom");
    retval.put("US", "United States");
    return retval;
  }

  static Map<String, List<String>> getCityMap() {
    List<String> beCities = new ArrayList<String>();
    beCities.add("Antwerp");
    beCities.add("Bruges");
    beCities.add("Brussels");
    beCities.add("Ghent");
    List<String> nlCities = new ArrayList<String>();
    nlCities.add("Amsterdam");
    nlCities.add("Eindhoven");
    nlCities.add("Rotterdam");
    nlCities.add("The Hague");
    List<String> ukCities = new ArrayList<String>();
    ukCities.add("London");
    ukCities.add("Bristol");
    ukCities.add("Oxford");
    ukCities.add("Stonehenge");
    List<String> usCities = new ArrayList<String>();
    usCities.add("Boston");
    usCities.add("Chicago");
    usCities.add("Los Angeles");
    usCities.add("New York");
    Map<String, List<String>> retval = new HashMap<String, List<String>>();
    retval.put("BE", beCities);
    retval.put("NL", nlCities);
    retval.put("UK", ukCities);
    retval.put("US", usCities);
    return retval;
  }
}
