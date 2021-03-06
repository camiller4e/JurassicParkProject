package controllers;

import db.DBHelper;
import db.DBPaddock;
import db.DBPark;
import db.Seeds;
import models.Dinosaur;
import models.Paddock;
import models.Park;
import models.Visitor;
import spark.ModelAndView;
import spark.template.velocity.VelocityTemplateEngine;

import java.util.*;


import static spark.Spark.get;
import static spark.Spark.modelAndView;
import static spark.Spark.post;
import static spark.SparkBase.staticFileLocation;
import static spark.route.HttpMethod.get;
import static spark.route.HttpMethod.patch;

public class ParkController {

    public static void main(String[] args) {
        VelocityTemplateEngine velocityTemplateEngine = new VelocityTemplateEngine();
        staticFileLocation("/public");
        Seeds.seedData();
        DinoController dinoController = new DinoController();
        PaddockController paddockController = new PaddockController();

        get ("/parks", (req, res) -> {
            rotatingDoors();
            HashMap<String, Object> model = new HashMap<>();
            List<Park> parks = DBHelper.getAll(Park.class);
            for (Park park : parks){
                park.checkRampage();
            }
            model.put("parks", parks);
            model.put("template", "park/index.vtl");
            return new ModelAndView(model, "layout.vtl");
        }, velocityTemplateEngine);

        get ("/", (req, res) -> {
            HashMap<String, Object> model = new HashMap<>();
            List<Park> parks = DBHelper.getAll(Park.class);
            List<Park> dinosaurs = DBHelper.getAll(Dinosaur.class);
            List<Park> paddocks = DBHelper.getAll(Paddock.class);
            model.put("parks", parks);
            model.put("dinosaurs", dinosaurs);
            model.put("paddocks", paddocks);
            model.put("template", "home.vtl");
            return new ModelAndView(model, "layout.vtl");
        }, velocityTemplateEngine);

        get ("/livefeed", (req, res) -> {
            HashMap<String, Object> model = new HashMap<>();
            model.put("template", "livefeed.vtl");
            return new ModelAndView(model, "layout.vtl");
        }, velocityTemplateEngine);

        get("/park/:id/newPaddock", (req, res) -> {
            int id = Integer.parseInt(req.params(":id"));
            HashMap<String, Object> model = new HashMap<>();
            model.put("template", "paddock/create.vtl");
            model.put("id", id);
            return new ModelAndView(model, "layout.vtl");
        }, velocityTemplateEngine);

        get("/park/new", (req, res) -> {
            HashMap<String, Object> model = new HashMap<>();
            model.put("template", "park/create.vtl");
            return new ModelAndView(model, "layout.vtl");
        }, velocityTemplateEngine);


        post("/park/new", (req, res) -> {
            String name = req.queryParams("name");
            Park newPark = new Park(name);
            DBHelper.save(newPark);
            res.redirect("/parks");
            return null;
        }, velocityTemplateEngine);

        get("/park/:id/update", (req, res) -> {
            int id = Integer.parseInt(req.params("id"));
            Park park = DBHelper.find(Park.class, id);
            List<Paddock> paddocks = DBPaddock.allPaddocks(park);
            HashMap<String, Object> model = new HashMap<>();
            model.put("template", "park/update.vtl");
            model.put("park", park);
            model.put("paddocks", paddocks);
            return new ModelAndView(model, "layout.vtl");
        }, velocityTemplateEngine);

        post("/park/:id/addVisitor", (req, res) -> {
            int id = Integer.parseInt(req.params(":id"));
            Park park = DBHelper.find(Park.class, id);
           for(int i = Integer.parseInt(req.queryParams("visitorNumber")); i > 0; --i){
               Visitor visitor = new Visitor("Tommy");
               park.addVisitor(visitor);
               DBHelper.save(visitor);
               DBHelper.update(park);
           }
            res.redirect("/parks");
            return null;
        }, velocityTemplateEngine);

        post("/park/:id/delete", (req, res) -> {
            int id = Integer.parseInt(req.params(":id"));
            Park park = DBHelper.find(Park.class, id);
            DBHelper.delete(park);
            res.redirect("/parks");
            return null;
        }, velocityTemplateEngine);

        post("/park/:id/update", (req, res) -> {
            int id = Integer.parseInt(req.params(":id"));
            String newName = req.queryParams("name");
            Park park = DBHelper.find(Park.class, id);
            park.setName(newName);
            DBHelper.update(park);
            res.redirect("/parks");
            return null;
        }, velocityTemplateEngine);

        post("/paddock/new", (req, res) -> {
            int parkId = Integer.parseInt(req.queryParams("park_id"));
            int newPadNum = Integer.parseInt(req.queryParams("pad_num"));
            Park park = DBHelper.find(Park.class, parkId);
            Paddock newPad = new Paddock(newPadNum);
            newPad.setPark(park);
            DBHelper.save(newPad);
            res.redirect("/parks");
            return null;
        }, velocityTemplateEngine);

    }

    public static void rotatingDoors(){
        Random r = new Random();
        List<Visitor> visitors = DBHelper.getAll(Visitor.class);
        if (visitors.size() > 1) {
            int low = 1;
            int high = visitors.size();
            int index = r.nextInt((high - low) +1) + low;
            if (index < visitors.size()) {
                DBHelper.delete(visitors.get(index));
            }
        }




    }
}
