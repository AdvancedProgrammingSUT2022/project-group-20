package ir.ap.controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import ir.ap.model.*;
import ir.ap.model.Tile.TileKnowledge;

public class UnitController extends AbstractGameController {
    public UnitController(GameArea gameArea) {
        super(gameArea);
    }

    public Set<Tile> getUnitVisitingTiles(Unit unit) {
        if (unit == null) return null;
        Tile tile = unit.getTile();
        if (tile == null) return null;
        Set<Tile> retTiles = new HashSet<>();
        for (Tile adjTile : tile.getNeighbors()) {
            retTiles.add(adjTile);
            if (!adjTile.isBlock()) {
                for (Tile tileInDepth2 : adjTile.getNeighbors()) {
                    retTiles.add(tileInDepth2);
                }
            }
        }
        return retTiles;
    }

    public void addUnit(Civilization civilization, Tile tile, UnitType unitType){
        Unit unit = new Unit(unitType, civilization, tile);
        civilization.addUnit(unit);
        addUnitToMap(unit);
    }

    public boolean addUnitToMap(Unit unit) {
        if (unit == null)
            return false;
        Tile tile = unit.getTile();
        if (tile == null)
            return false;
        if (unit.isCivilian()) {
            tile.setNonCombatUnit(unit);
        } else {
            tile.setCombatUnit(unit);
        }
        for (Tile visitingTile : getUnitVisitingTiles(unit)) {
            visitingTile.addVisitingUnit(unit);
            gameArea.setTileKnowledgeByCivilization(unit.getCivilization(), visitingTile, TileKnowledge.VISIBLE);
        }
        return true;
    }

    public boolean removeUnitFromMap(Unit unit) {
        if (unit == null) return false;
        Tile tile = unit.getTile();
        if (tile == null) return false;
        if (unit.isCivilian()) {
            tile.setNonCombatUnit(null);
        } else {
            tile.setCombatUnit(null);
        }
        for (Tile visitingTile : getUnitVisitingTiles(unit)) {
            visitingTile.removeVisitingUnit(unit);
            if (!visitingTile.civilizationIsVisiting(unit.getCivilization())) {
                gameArea.setTileKnowledgeByCivilization(unit.getCivilization(), visitingTile, TileKnowledge.REVEALED);
            }
        }
        return true;
    }

    public boolean unitMoveTo(Civilization civilization, Tile target)
    {
        if (civilization == null || target == null) return false;
        Unit unit = civilization.getSelectedUnit();
        if(unit == null) return false;
        Tile tile = unit.getTile();
        if (tile == null) return false;
        int dist = gameArea.getWeightedDistance(tile, target);
        if (unit.getMp() == 0 || dist >= unit.getMp() + UnitType.MAX_MOVEMENT)
            return false;
        if ((unit.isCivilian() && target.getNonCombatUnit() != null) ||
            (!unit.isCivilian() && target.getCombatUnit() != null))
            return false;
        removeUnitFromMap(unit);
        unit.addToMp(-dist);
        unit.setTile(target);
        unit.setUnitAction(UnitType.UnitAction.MOVETO);
        addUnitToMap(unit);
        return true;
    }

    public boolean unitSleep(Civilization civilization)
    {
        Unit unit = civilization.getSelectedUnit();
        if(unit == null) return false;
        unit.setUnitAction(UnitType.UnitAction.SLEEP);
        return true;
    }

    public boolean unitFortify(Civilization civilization)
    {
        Unit unit = civilization.getSelectedUnit();
        if(unit == null) return false;
        if(unit.getUnitType().getCombatType() == UnitType.CombatType.CIVILIAN) return false;
        if(unit.getUnitType().getCombatType() == UnitType.CombatType.MOUNTED)  return false;
        if(unit.getUnitType().getCombatType() == UnitType.CombatType.ARMORED) return false;
        unit.setUnitAction(UnitType.UnitAction.FORTIFY);
        return true;
    }

    public boolean unitGarrison(Civilization civilization)
    {
        Unit unit = civilization.getSelectedUnit();
        if(unit == null) return false;
        City city = unit.getTile().getCity();
        if (city == null || city.getCivilization() != civilization || city.getCombatUnit() != unit) return false;
        unit.setUnitAction(UnitType.UnitAction.GARRISON);
        return true;
    }
    public boolean unitSetupForRangedAttack(Civilization civilization)
    {
        Unit unit = civilization.getSelectedUnit();
        if(unit == null) return false;

        unit.setUnitAction(UnitType.UnitAction.SETUP_RANGED);
        return true;
    }

    public boolean unitAttack(Civilization civilization, Tile target)
    {
        if (civilization == null || target == null) return false;
        Unit unit = civilization.getSelectedUnit();
        City city = civilization.getSelectedCity();
        Unit enemyUnit = target.getCombatUnit();
        City enemyCity = target.getCity();
        if (enemyUnit == null) enemyUnit = target.getNonCombatUnit();
        Tile curTile = (unit == null ? (city == null ? null : city.getTile()) : unit.getTile());
        if((unit == null && city == null) || (enemyUnit == null && enemyCity == null) || curTile == null) return false;
        Civilization otherCiv = (enemyUnit == null ? (enemyCity == null ? null : enemyCity.getCivilization()) : enemyUnit.getCivilization());
        if (otherCiv == null) return false;

        if(city != null) {
            if (enemyCity.getTile() == target) {
                if (gameArea.getDistance > city.getRange()) return false;
                enemyCity.setHp(enemyCity.getHp() - city.getCombatStrength());

                if (enemyCity.getHp() <= 0) {
                    enemyCity.setHp(0);
                }

                return true;
            }
            if(enemyUnit != null){
                if (gameArea.getDistance > city.getRange()) return false;
                enemyUnit.setHp(enemyUnit.getHp() - city.getCombatStrength());

                if (enemyUnit.getHp() <= 0) {
                    otherCiv.removeUnit(enemyUnit);
                }

                return true;
            }
        }

        if(unit != null) {
            if(unit.getUnitType().getCombatType() == UnitType.CombatType.CIVILIAN) return false;
            if(enemyUnit != null) {
                if (unit.getCombatType() == UnitType.CombatType.ARCHERY || unit.getCombatType() == UnitType.CombatType.SIEGE) {
                    if (gameArea.getDistanceInTiles(curTile, target) > unit.getRange()) return false;
                    if (unit.getCombatType() == UnitType.CombatType.SIEGE && unit.getUnitAction() != UnitType.UnitAction.SETUP_RANGED)
                        return false;
                    enemyUnit.setHp(enemyUnit.getHp() - unit.getCombatStrength());
                    if (enemyUnit.isDead()) {
                        otherCiv.removeUnit(enemyUnit);
                        target.setCombatUnit(null);
                    }
                    if (unit.isDead()) {
                        civilization.removeUnit(unit);
                        curTile.setCombatUnit(null);
                    }
                    return true;
                    // in this type of attack we will kill worker
                }

                if (unit.getCombatType() == UnitType.CombatType.MOUNTED || unit.getCombatType() == UnitType.CombatType.MELEE || unit.getCombatType() == UnitType.CombatType.GUNPOWDER || unit.getCombatType() == UnitType.CombatType.ARMORED || unit.getCombatType() == UnitType.CombatType.RECON) {
                    if (gameArea.getDistance > unit.getMp()) return false;
                    if (enemyUnit.getUnitType().getCombatType() == UnitType.CombatType.CIVILIAN) {
                        if (enemyCity.getTile() != target) {
                            otherCiv.removeUnit(enemyUnit);
                            civilization.addUnit(enemyUnit);
                            return true;
                        }
                    } else {
                        enemyUnit.setHp(enemyUnit.getHp() - unit.getCombatStrength());
                        unit.setHp(unit.getHp() - enemyUnit.getCombatStrength());
                        if (enemyUnit.getHp() <= 0) {
                            unitMoveTo(civilization, target);
                            otherCiv.removeUnit(enemyUnit);
                        }
                        if (unit.getHp() <= 0) {
                            civilization.removeUnit(unit);
                        }
                        return true;
                    }
                    // in this type of attack we got worker if it is not city
                }
            }
            if (enemyCity.getTile() == target) {
                if (unit.getCombatType() == UnitType.CombatType.ARCHERY || unit.getCombatType() == UnitType.CombatType.SIEGE) {
                    if (gameArea.getDistance > unit.getRange()) return false;
                    if (unit.getCombatType() == UnitType.CombatType.SIEGE && unit.getUnitAction() != UnitType.UnitAction.SETUP_RANGED)
                        return false;
                    enemyCity.setHp(enemyCity.getHp() - unit.getCombatStrength());
                    unit.setHp(unit.getHp() - enemyCity.getCombatStrength());

                    if (enemyCity.getHp() <= 0) {
                        enemyCity.setHp(0);
                    }
                    if (unit.getHp() <= 0) {
                        civilization.removeUnit(unit);
                    }
                    return true;
                }

                if (unit.getCombatType() == UnitType.CombatType.MOUNTED || unit.getCombatType() == UnitType.CombatType.MELEE || unit.getCombatType() == UnitType.CombatType.GUNPOWDER || unit.getCombatType() == UnitType.CombatType.ARMORED || unit.getCombatType() == UnitType.CombatType.RECON) {
                    if (gameArea.getDistance > unit.getMovement()) return false;
                    enemyCity.setHp(enemyCity.getHp() - unit.getCombatStrength());
                    unit.setHp(unit.getHp() - enemyCity.getCombatStrength());
                    if (enemyCity.getHp() <= 0) {
                        unitMoveTo(civilization, target);
                        otherCiv.removeCity(enemyCity);
                        civilization.addCity(enemyCity);
                        if(enemyUnit.getUnitType().getCombatType() == UnitType.CombatType.CIVILIAN)
                        {
                            otherCiv.removeUnit(enemyUnit);
                            civilization.addUnit(enemyUnit);
                        }
                    }
                    if (unit.getHp() <= 0) {
                        civilization.removeUnit(unit);
                    }
                    return true;
                }
            }
            if (unit.getUnitType().getCombatType() != UnitType.CombatType.ARMORED && unit.getUnitType().getCombatType() != UnitType.CombatType.MOUNTED)
                unit.setMp(0);
            else
                unit.setMp(unit.getMp() - gameArea.getDistance);


            unit.setUnitAction(UnitType.UnitAction.ATTACK);
        }
        return false;
    }

    public boolean unitFoundCity(Civilization civilization)
    {
        if (civilization == null) return false;
        Unit unit = civilization.getSelectedUnit();
        if (unit == null || !(unit.getUnitType() == UnitType.SETTLER))
            return false;
        Tile target = unit.getTile();
        if (target == null || target.getCity() != null)
            return false;
        City city;
        int cnt = 10;
        do {
            city = new City(City.getCityName(RANDOM.nextInt()), civilization, target);
        } while (!cityController.addCity(city) && cnt --> 0);
        unit.setUnitAction(UnitType.UnitAction.FOUND_CITY);
        return true;
    }

    public boolean unitCancelMission(Civilization civilization)
    {
        Unit unit = civilization.getSelectedUnit();
        if(unit == null) return false;

        unit.setUnitAction(UnitType.UnitAction.CANCEL_MISSION);
        return true;
    }

    public boolean unitBuildRoad(Civilization civilization)
    {
        Unit unit = civilization.getSelectedUnit();
        if(unit == null) return false;

        unit.setUnitAction(UnitType.UnitAction.BUILD_ROAD);
        return true;
    }

    public boolean unitBuildRailRoad(Civilization civilization)
    {
        Unit unit = civilization.getSelectedUnit();
        if(unit == null) return false;

        unit.setUnitAction(UnitType.UnitAction.BUILD_RAILROAD);
        return true;
    }

    public boolean unitBuildImprovement(Civilization civilization, Improvement improvement)
    {
        Unit unit = civilization.getSelectedUnit();
        if(unit == null) return false;

        if(improvement == Improvement.FARM)
            unit.setUnitAction(UnitType.UnitAction.BUILD_FARM);
        if(improvement == Improvement.MINE)
            unit.setUnitAction(UnitType.UnitAction.BUILD_MINE);
        if(improvement == Improvement.TRADING_POST)
            unit.setUnitAction(UnitType.UnitAction.BUILD_TRADINGPOST);
        if(improvement == Improvement.LUMBER_MILL)
            unit.setUnitAction(UnitType.UnitAction.BUILD_LUMBERMILL);
        if(improvement == Improvement.PASTURE)
            unit.setUnitAction(UnitType.UnitAction.BUILD_PASTURE);
        if(improvement == Improvement.CAMP)
            unit.setUnitAction(UnitType.UnitAction.BUILD_CAMP);
        if(improvement == Improvement.PLANTATION)
            unit.setUnitAction(UnitType.UnitAction.BUILD_PLANTATION);
        if(improvement == Improvement.QUARRY)
            unit.setUnitAction(UnitType.UnitAction.BUILD_QUARRY);
        return true;
    }
    public boolean unitRemoveJungle(Civilization civilization)
    {
        Unit unit = civilization.getSelectedUnit();
        if(unit == null) return false;

        unit.setUnitAction(UnitType.UnitAction.REMOVE_JUNGLE);
        return true;
    }
    public boolean unitRemoveForest(Civilization civilization)
    {
        Unit unit = civilization.getSelectedUnit();
        if(unit == null) return false;

        unit.setUnitAction(UnitType.UnitAction.REMOVE_FOREST);
        return true;
    }
    public boolean unitRemoveRoute(Civilization civilization)
    {
        Unit unit = civilization.getSelectedUnit();
        if(unit == null) return false;

        unit.setUnitAction(UnitType.UnitAction.REMOVE_ROUTE);
        return true;
    }

    public boolean unitRepair(Civilization civilization)
    {
        Unit unit = civilization.getSelectedUnit();
        if(unit == null) return false;

        unit.setUnitAction(UnitType.UnitAction.REPAIR);
        return true;
    }
}
