-- Seeds gallery_items/gallery_categories with the images that were, until now, hardcoded
-- in zebratravel/app/Dados/gallery.tsx, so the public /galeria page keeps showing the
-- same photos once it switches from that static file to GET /api/gallery. Guarded with
-- NOT EXISTS so this stays safe to re-run (see dev-notes.md #5 on non-atomic migrations).

INSERT INTO gallery_categories (name)
SELECT v.name FROM (VALUES ('activity'), ('destination'), ('tours')) AS v(name)
ON CONFLICT (name) DO NOTHING;

INSERT INTO gallery_items (img_src)
SELECT v.img_src FROM (VALUES
    ('/images/resource/gallery-10.jpg'),
    ('/images/resource/gallery-11.jpg'),
    ('/images/resource/gallery-12.jpg'),
    ('/images/resource/gallery-13.jpg'),
    ('/images/resource/gallery-14.jpg'),
    ('/images/resource/gallery-15.jpg'),
    ('/images/resource/gallery-16.jpg'),
    ('/images/resource/gallery-17.jpg'),
    ('/images/resource/gallery-18.jpg'),
    ('/images/resource/gallery-19.jpg'),
    ('/images/resource/gallery-20.jpg'),
    ('/images/resource/gallery-21.jpg')
) AS v(img_src)
WHERE NOT EXISTS (SELECT 1 FROM gallery_items gi WHERE gi.img_src = v.img_src);

INSERT INTO gallery_item_categories (gallery_item_id, category_id)
SELECT gi.id, gc.id
FROM (VALUES
    ('/images/resource/gallery-10.jpg', 'tours'),
    ('/images/resource/gallery-11.jpg', 'destination'),
    ('/images/resource/gallery-12.jpg', 'activity'),
    ('/images/resource/gallery-13.jpg', 'activity'),
    ('/images/resource/gallery-13.jpg', 'tours'),
    ('/images/resource/gallery-14.jpg', 'activity'),
    ('/images/resource/gallery-14.jpg', 'destination'),
    ('/images/resource/gallery-15.jpg', 'destination'),
    ('/images/resource/gallery-15.jpg', 'tours'),
    ('/images/resource/gallery-16.jpg', 'activity'),
    ('/images/resource/gallery-16.jpg', 'destination'),
    ('/images/resource/gallery-17.jpg', 'activity'),
    ('/images/resource/gallery-17.jpg', 'tours'),
    ('/images/resource/gallery-18.jpg', 'destination'),
    ('/images/resource/gallery-18.jpg', 'tours'),
    ('/images/resource/gallery-19.jpg', 'activity'),
    ('/images/resource/gallery-19.jpg', 'destination'),
    ('/images/resource/gallery-20.jpg', 'activity'),
    ('/images/resource/gallery-20.jpg', 'tours'),
    ('/images/resource/gallery-21.jpg', 'destination'),
    ('/images/resource/gallery-21.jpg', 'tours')
) AS v(img_src, category_name)
JOIN gallery_items gi ON gi.img_src = v.img_src
JOIN gallery_categories gc ON gc.name = v.category_name
WHERE NOT EXISTS (
    SELECT 1 FROM gallery_item_categories gic
    WHERE gic.gallery_item_id = gi.id AND gic.category_id = gc.id
);
