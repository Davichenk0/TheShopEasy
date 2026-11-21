-- Corrige las URLs de las imágenes de la tabla "fruits".
-- Las URLs estaban guardadas con formato markdown ([url](url), [url], (url))
-- y dos apuntaban a archivos que no existen en el bucket (extensión/nombre incorrectos).
--
-- Ejecutar en Supabase Dashboard > SQL Editor.

update public.fruits
set img_url = 'https://octwtjovandhczqazvgr.supabase.co/storage/v1/object/public/fruits/apple.jpg'
where id = 1;

update public.fruits
set img_url = 'https://octwtjovandhczqazvgr.supabase.co/storage/v1/object/public/fruits/banana.jpg'
where id = 2;

update public.fruits
set img_url = 'https://octwtjovandhczqazvgr.supabase.co/storage/v1/object/public/fruits/pineaple.jpg'
where id = 3;

update public.fruits
set img_url = 'https://octwtjovandhczqazvgr.supabase.co/storage/v1/object/public/fruits/strawberry.jpg'
where id = 4;

-- Verificación: los cuatro valores deben ser URLs limpias que empiecen por https://
select id, name, img_url from public.fruits order by id;
