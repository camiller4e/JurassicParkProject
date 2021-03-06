package controllers;

import db.DBHelper;
import db.DBPaddock;
import models.*;
import spark.ModelAndView;
import spark.template.velocity.VelocityTemplateEngine;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static spark.Spark.get;
import static spark.Spark.post;

public class DinoController {

    public DinoController(){
        this.setupEndPoints();
    }

    private void setupEndPoints(){

        get("/dinosaurs", (req, res) -> {
            HashMap<String, Object> model = new HashMap<>();
            List<Dinosaur> dinosaurs = DBHelper.getAll(Dinosaur.class);
            model.put("template", "dinosaurs/index.vtl");
            model.put("dinosaurs", dinosaurs);
            return new ModelAndView(model, "layout.vtl");

        }, new VelocityTemplateEngine());


        get("/dinosaurs/new", (req, res) -> {
            HashMap<String, Object> model = new HashMap<>();
            List<Paddock> paddocks = DBHelper.getAll(Paddock.class);
            HashMap<String, String> species = new HashMap<>();
            species.put("Tyrannosaurus Rex", "TREX");
            species.put("Velociraptor", "RAPTOR");
            species.put("Allosaurus", "ALLOSAURUS");
            species.put("Stegosaurus", "STEGOSAURUS");
            species.put("Minmi", "MINMI");
            species.put("Triceratops", "TRICERATOPS");
            model.put("species", species);
            model.put("paddocks", paddocks);
            model.put("template", "dinosaurs/create.vtl");
            return new ModelAndView(model, "layout.vtl");
        }, new VelocityTemplateEngine());

        get("/dinosaurs/:id/edit", (req, res) -> {
            int dinosaurId = Integer.parseInt(req.params(":id"));
            HashMap<String, Object> model = new HashMap<>();
            Dinosaur dinosaur = DBHelper.find(Dinosaur.class, dinosaurId);
            List<Paddock> paddocks = DBHelper.getAll(Paddock.class);
            List<Paddock> paddockClone = paddocks;

            for (int i = paddocks.size() -1; i >= 0; --i){
                List<Dinosaur> list = DBPaddock.getDinosInPaddock(paddocks.get(i));
                if (list.size() > 0){
                    String type = list.get(0).getClass().toString();
                    String type2 = dinosaur.getClass().toString();
                if ((list.get(0).getClass() != dinosaur.getClass())){
                    paddockClone.remove(paddocks.get(i));
                }
                }
            }
            model.put("paddocks", paddockClone);
            model.put("dinosaur", dinosaur);
            model.put("template", "dinosaurs/edit.vtl");
            return new ModelAndView(model, "layout.vtl");
        }, new VelocityTemplateEngine());

        get("/dinosaurs/:id/feed", (req, res) ->{
            int dinosaurId = Integer.parseInt(req.params(":id"));
            HashMap<String, Object> model = new HashMap<>();
            Dinosaur dinosaur = DBHelper.find(Dinosaur.class, dinosaurId);
            List<FoodType> food = new ArrayList<>();
            if (dinosaur.getClass().toString().equals("class models.Herbivore")){
                food.add(FoodType.CARROT);
                food.add(FoodType.TURNIP);
            } else {
                food.add(FoodType.SHEEP);
                food.add(FoodType.GOAT);
            }
            IEat hungry = (IEat) dinosaur;
            model.put("food", food);
            model.put("hungry", hungry);
            model.put("dinosaur", dinosaur);
            model.put("template", "dinosaurs/feed.vtl");
            return new ModelAndView(model, "layout.vtl");
        }, new VelocityTemplateEngine());

        post("/dinosaurs/new", (req, res) -> {
            int paddockId = Integer.parseInt(req.queryParams("paddock"));
            Paddock paddock = DBHelper.find(Paddock.class, paddockId);
            Species species =  Enum.valueOf(Species.class, req.queryParams("species"));
            if (species == Species.TREX || species == Species.RAPTOR || species == Species.ALLOSAURUS){
                Carnivore newCarnivore = new Carnivore(species, true);
                newCarnivore.assignPaddock(paddock);
                DBHelper.save(newCarnivore);
            } else {
                Herbivore newHerbivore = new Herbivore(species, true);
                newHerbivore.assignPaddock(paddock);
                DBHelper.save(newHerbivore);
            }
            res.redirect("/dinosaurs");
            return null;
        }, new VelocityTemplateEngine());

        post("/dinosaurs/:id/delete", (req, res) -> {
            int dinosaurId = Integer.parseInt(req.params("id"));
            Dinosaur foundDinosaur = DBHelper.find(Dinosaur.class, dinosaurId);
            DBHelper.delete(foundDinosaur);
            res.redirect("/dinosaurs");
            return null;
        }, new VelocityTemplateEngine());

        post("/dinosaurs/:id/edit", (req, res) -> {
            int dinosaurId = Integer.parseInt(req.params("id"));
            Dinosaur dinosaur = DBHelper.find(Dinosaur.class, dinosaurId);

            int paddockId = Integer.parseInt(req.queryParams("paddock"));
            Paddock paddock = DBHelper.find(Paddock.class, paddockId);

            dinosaur.setPaddock(paddock);

            DBHelper.update(dinosaur);
            res.redirect("/dinosaurs");
            return null;
        }, new VelocityTemplateEngine());

        post("/dinosaurs/:id/feed", (req, res) ->{
            Enum<FoodType> foodTypeEnum;
            int dinosaurId = Integer.parseInt(req.params(":id"));
            Dinosaur dinosaur = DBHelper.find(Dinosaur.class, dinosaurId);
            String food = req.queryParams("food");
            IEat hungryDino = (IEat) dinosaur;
            if (food.equals("Carrot")){
                hungryDino.eat(FoodType.CARROT);
            } else if (food.equals("Turnip")){
                hungryDino.eat(FoodType.TURNIP);
            }else if (food.equals("Sheep")){
                hungryDino.eat(FoodType.SHEEP);
            }else{
                hungryDino.eat(FoodType.GOAT);
            }
            DBHelper.update(dinosaur);
            res.redirect("/dinosaurs");
            return null;
        }, new VelocityTemplateEngine());


        get("/dinosaurs/:id", (req, res) -> {
            HashMap<String, Object> model = new HashMap<>();
            int dinosaurId = Integer.parseInt(req.params("id"));
            Dinosaur foundDinosaur = DBHelper.find(Dinosaur.class, dinosaurId);
            List<Paddock> paddocks = DBHelper.getAll(Paddock.class);
            model.put("dinosaur", foundDinosaur);
            model.put("paddocks", paddocks);
            model.put("template", "dinosaurs/show.vtl");
            return new ModelAndView(model, "layout.vtl");

        }, new VelocityTemplateEngine());






    }
}
