package ir.ap.controller;

import ir.ap.model.City;
import ir.ap.model.Civilization;
import ir.ap.model.GameArea;
import ir.ap.model.Tile;

public class CityController extends AbstractGameController {
    public CityController(GameArea gameArea) {
        super(gameArea);
    }

    public boolean addCity(City city) {
        Tile tile = city.getTile();
        Civilization civ = city.getCivilization();
        if (tile == null || tile.getCity() != null)
            return false;
        if (!City.addCity(city))
            return false;
        tile.setCity(city);
        civ.addCity(city);
        city.setCombatUnit(tile.getCombatUnit());
        city.setNonCombatUnit(tile.getNonCombatUnit());
        for (Tile territoryTile : gameArea.getTilesInRange(city, city.getTerritoryRange())) {
            city.addToTerritory(territoryTile);
        }
        // TODO: TileKnowledges
        return true;
    }
}
