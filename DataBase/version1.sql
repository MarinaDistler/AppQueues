PGDMP                         z         	   AppQueues    15.1    15.1 3    6           0    0    ENCODING    ENCODING        SET client_encoding = 'UTF8';
                      false            7           0    0 
   STDSTRINGS 
   STDSTRINGS     (   SET standard_conforming_strings = 'on';
                      false            8           0    0 
   SEARCHPATH 
   SEARCHPATH     8   SELECT pg_catalog.set_config('search_path', '', false);
                      false            9           1262    16399 	   AppQueues    DATABASE        CREATE DATABASE "AppQueues" WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE_PROVIDER = libc LOCALE = 'Russian_Russia.1251';
    DROP DATABASE "AppQueues";
                root    false            �            1259    16484    history_work    TABLE     �   CREATE TABLE public.history_work (
    record_id integer NOT NULL,
    worker_user_id integer NOT NULL,
    start_time timestamp with time zone DEFAULT now() NOT NULL,
    end_time timestamp with time zone
);
     DROP TABLE public.history_work;
       public         heap    postgres    false            �            1259    16483    history_work_record_id_seq    SEQUENCE     �   CREATE SEQUENCE public.history_work_record_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 1   DROP SEQUENCE public.history_work_record_id_seq;
       public          postgres    false    223            :           0    0    history_work_record_id_seq    SEQUENCE OWNED BY     Y   ALTER SEQUENCE public.history_work_record_id_seq OWNED BY public.history_work.record_id;
          public          postgres    false    222            �            1259    16432    queue_users    TABLE     o  CREATE TABLE public.queue_users (
    record_id integer NOT NULL,
    user_id integer NOT NULL,
    queue_id integer NOT NULL,
    properties json,
    creation_time timestamp with time zone DEFAULT now() NOT NULL,
    start_work_time timestamp with time zone,
    end_work_time timestamp with time zone,
    status character varying DEFAULT 'WAIT'::text NOT NULL
);
    DROP TABLE public.queue_users;
       public         heap    postgres    false            �            1259    16431    queue_users_record_id_seq    SEQUENCE     �   CREATE SEQUENCE public.queue_users_record_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 0   DROP SEQUENCE public.queue_users_record_id_seq;
       public          postgres    false    217            ;           0    0    queue_users_record_id_seq    SEQUENCE OWNED BY     W   ALTER SEQUENCE public.queue_users_record_id_seq OWNED BY public.queue_users.record_id;
          public          postgres    false    216            �            1259    16456    queue_workers    TABLE     �   CREATE TABLE public.queue_workers (
    record_id integer NOT NULL,
    queue_id integer NOT NULL,
    worker_user_id integer NOT NULL,
    insert_time timestamp with time zone DEFAULT now() NOT NULL,
    delete_time timestamp with time zone NOT NULL
);
 !   DROP TABLE public.queue_workers;
       public         heap    postgres    false            �            1259    16455    queue_workers_record_id_seq    SEQUENCE     �   CREATE SEQUENCE public.queue_workers_record_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 2   DROP SEQUENCE public.queue_workers_record_id_seq;
       public          postgres    false    221            <           0    0    queue_workers_record_id_seq    SEQUENCE OWNED BY     [   ALTER SEQUENCE public.queue_workers_record_id_seq OWNED BY public.queue_workers.record_id;
          public          postgres    false    220            �            1259    16443    queues    TABLE     �   CREATE TABLE public.queues (
    queue_id integer NOT NULL,
    name character varying NOT NULL,
    owner_user_id integer NOT NULL,
    properties json,
    number integer NOT NULL
);
    DROP TABLE public.queues;
       public         heap    postgres    false            �            1259    16441    queues_queue_id_seq    SEQUENCE     �   CREATE SEQUENCE public.queues_queue_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 *   DROP SEQUENCE public.queues_queue_id_seq;
       public          postgres    false    219            =           0    0    queues_queue_id_seq    SEQUENCE OWNED BY     K   ALTER SEQUENCE public.queues_queue_id_seq OWNED BY public.queues.queue_id;
          public          postgres    false    218            �            1259    16419    users    TABLE     �  CREATE TABLE public.users (
    login character varying NOT NULL,
    password character varying NOT NULL,
    insert_time timestamp with time zone DEFAULT now() NOT NULL,
    user_id integer NOT NULL,
    name character varying,
    email character varying,
    status character varying DEFAULT 'NOT_VERIFIED'::text NOT NULL,
    qr_code character varying DEFAULT 'NOT_DONE'::text NOT NULL
);
    DROP TABLE public.users;
       public         heap    postgres    false            �            1259    16418    users_user_id_seq    SEQUENCE     �   CREATE SEQUENCE public.users_user_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 (   DROP SEQUENCE public.users_user_id_seq;
       public          postgres    false    215            >           0    0    users_user_id_seq    SEQUENCE OWNED BY     G   ALTER SEQUENCE public.users_user_id_seq OWNED BY public.users.user_id;
          public          postgres    false    214            �           2604    16618    history_work record_id    DEFAULT     �   ALTER TABLE ONLY public.history_work ALTER COLUMN record_id SET DEFAULT nextval('public.history_work_record_id_seq'::regclass);
 E   ALTER TABLE public.history_work ALTER COLUMN record_id DROP DEFAULT;
       public          postgres    false    222    223    223            }           2604    16619    queue_users record_id    DEFAULT     ~   ALTER TABLE ONLY public.queue_users ALTER COLUMN record_id SET DEFAULT nextval('public.queue_users_record_id_seq'::regclass);
 D   ALTER TABLE public.queue_users ALTER COLUMN record_id DROP DEFAULT;
       public          postgres    false    216    217    217            �           2604    16620    queue_workers record_id    DEFAULT     �   ALTER TABLE ONLY public.queue_workers ALTER COLUMN record_id SET DEFAULT nextval('public.queue_workers_record_id_seq'::regclass);
 F   ALTER TABLE public.queue_workers ALTER COLUMN record_id DROP DEFAULT;
       public          postgres    false    221    220    221            �           2604    16621    queues queue_id    DEFAULT     r   ALTER TABLE ONLY public.queues ALTER COLUMN queue_id SET DEFAULT nextval('public.queues_queue_id_seq'::regclass);
 >   ALTER TABLE public.queues ALTER COLUMN queue_id DROP DEFAULT;
       public          postgres    false    219    218    219            z           2604    16622    users user_id    DEFAULT     n   ALTER TABLE ONLY public.users ALTER COLUMN user_id SET DEFAULT nextval('public.users_user_id_seq'::regclass);
 <   ALTER TABLE public.users ALTER COLUMN user_id DROP DEFAULT;
       public          postgres    false    215    214    215            3          0    16484    history_work 
   TABLE DATA           W   COPY public.history_work (record_id, worker_user_id, start_time, end_time) FROM stdin;
    public          postgres    false    223   =       -          0    16432    queue_users 
   TABLE DATA           �   COPY public.queue_users (record_id, user_id, queue_id, properties, creation_time, start_work_time, end_work_time, status) FROM stdin;
    public          postgres    false    217   =       1          0    16456    queue_workers 
   TABLE DATA           f   COPY public.queue_workers (record_id, queue_id, worker_user_id, insert_time, delete_time) FROM stdin;
    public          postgres    false    221   <=       /          0    16443    queues 
   TABLE DATA           S   COPY public.queues (queue_id, name, owner_user_id, properties, number) FROM stdin;
    public          postgres    false    219   Y=       +          0    16419    users 
   TABLE DATA           d   COPY public.users (login, password, insert_time, user_id, name, email, status, qr_code) FROM stdin;
    public          postgres    false    215   v=       ?           0    0    history_work_record_id_seq    SEQUENCE SET     I   SELECT pg_catalog.setval('public.history_work_record_id_seq', 1, false);
          public          postgres    false    222            @           0    0    queue_users_record_id_seq    SEQUENCE SET     H   SELECT pg_catalog.setval('public.queue_users_record_id_seq', 1, false);
          public          postgres    false    216            A           0    0    queue_workers_record_id_seq    SEQUENCE SET     J   SELECT pg_catalog.setval('public.queue_workers_record_id_seq', 1, false);
          public          postgres    false    220            B           0    0    queues_queue_id_seq    SEQUENCE SET     B   SELECT pg_catalog.setval('public.queues_queue_id_seq', 1, false);
          public          postgres    false    218            C           0    0    users_user_id_seq    SEQUENCE SET     ?   SELECT pg_catalog.setval('public.users_user_id_seq', 3, true);
          public          postgres    false    214            �           2606    16490    history_work history_work_pk 
   CONSTRAINT     a   ALTER TABLE ONLY public.history_work
    ADD CONSTRAINT history_work_pk PRIMARY KEY (record_id);
 F   ALTER TABLE ONLY public.history_work DROP CONSTRAINT history_work_pk;
       public            postgres    false    223            �           2606    16470    queue_users queue_users_pk 
   CONSTRAINT     _   ALTER TABLE ONLY public.queue_users
    ADD CONSTRAINT queue_users_pk PRIMARY KEY (record_id);
 D   ALTER TABLE ONLY public.queue_users DROP CONSTRAINT queue_users_pk;
       public            postgres    false    217            �           2606    16463    queue_workers queue_workers_pk 
   CONSTRAINT     c   ALTER TABLE ONLY public.queue_workers
    ADD CONSTRAINT queue_workers_pk PRIMARY KEY (record_id);
 H   ALTER TABLE ONLY public.queue_workers DROP CONSTRAINT queue_workers_pk;
       public            postgres    false    221            �           2606    16451    queues queues_pk 
   CONSTRAINT     T   ALTER TABLE ONLY public.queues
    ADD CONSTRAINT queues_pk PRIMARY KEY (queue_id);
 :   ALTER TABLE ONLY public.queues DROP CONSTRAINT queues_pk;
       public            postgres    false    219            �           2606    16427    users users_pk 
   CONSTRAINT     Q   ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pk PRIMARY KEY (user_id);
 8   ALTER TABLE ONLY public.users DROP CONSTRAINT users_pk;
       public            postgres    false    215            �           2606    16494    users users_un 
   CONSTRAINT     J   ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_un UNIQUE (login);
 8   ALTER TABLE ONLY public.users DROP CONSTRAINT users_un;
       public            postgres    false    215            �           1259    16491    history_work_record_id_idx    INDEX     X   CREATE INDEX history_work_record_id_idx ON public.history_work USING btree (record_id);
 .   DROP INDEX public.history_work_record_id_idx;
       public            postgres    false    223            �           1259    16471    queue_users_record_id_idx    INDEX     V   CREATE INDEX queue_users_record_id_idx ON public.queue_users USING btree (record_id);
 -   DROP INDEX public.queue_users_record_id_idx;
       public            postgres    false    217            �           1259    16464    queue_workers_record_id_idx    INDEX     Z   CREATE INDEX queue_workers_record_id_idx ON public.queue_workers USING btree (record_id);
 /   DROP INDEX public.queue_workers_record_id_idx;
       public            postgres    false    221            �           1259    16452    queues_queue_id_idx    INDEX     J   CREATE INDEX queues_queue_id_idx ON public.queues USING btree (queue_id);
 '   DROP INDEX public.queues_queue_id_idx;
       public            postgres    false    219            �           1259    16428    users_user_id_idx    INDEX     M   CREATE UNIQUE INDEX users_user_id_idx ON public.users USING btree (user_id);
 %   DROP INDEX public.users_user_id_idx;
       public            postgres    false    215            �           2606    16643    history_work history_work_fk    FK CONSTRAINT     �   ALTER TABLE ONLY public.history_work
    ADD CONSTRAINT history_work_fk FOREIGN KEY (worker_user_id) REFERENCES public.users(user_id);
 F   ALTER TABLE ONLY public.history_work DROP CONSTRAINT history_work_fk;
       public          postgres    false    223    3206    215            �           2606    16648    queue_users queue_users_fk    FK CONSTRAINT     ~   ALTER TABLE ONLY public.queue_users
    ADD CONSTRAINT queue_users_fk FOREIGN KEY (user_id) REFERENCES public.users(user_id);
 D   ALTER TABLE ONLY public.queue_users DROP CONSTRAINT queue_users_fk;
       public          postgres    false    3206    217    215            �           2606    16653    queue_users queue_users_fk_1    FK CONSTRAINT     �   ALTER TABLE ONLY public.queue_users
    ADD CONSTRAINT queue_users_fk_1 FOREIGN KEY (queue_id) REFERENCES public.queues(queue_id);
 F   ALTER TABLE ONLY public.queue_users DROP CONSTRAINT queue_users_fk_1;
       public          postgres    false    3214    217    219            �           2606    16633    queue_workers queue_workers_fk    FK CONSTRAINT     �   ALTER TABLE ONLY public.queue_workers
    ADD CONSTRAINT queue_workers_fk FOREIGN KEY (queue_id) REFERENCES public.queues(queue_id);
 H   ALTER TABLE ONLY public.queue_workers DROP CONSTRAINT queue_workers_fk;
       public          postgres    false    221    219    3214            �           2606    16638     queue_workers queue_workers_fk_1    FK CONSTRAINT     �   ALTER TABLE ONLY public.queue_workers
    ADD CONSTRAINT queue_workers_fk_1 FOREIGN KEY (worker_user_id) REFERENCES public.users(user_id);
 J   ALTER TABLE ONLY public.queue_workers DROP CONSTRAINT queue_workers_fk_1;
       public          postgres    false    215    3206    221            �           2606    16628    queues queues_fk    FK CONSTRAINT     z   ALTER TABLE ONLY public.queues
    ADD CONSTRAINT queues_fk FOREIGN KEY (owner_user_id) REFERENCES public.users(user_id);
 :   ALTER TABLE ONLY public.queues DROP CONSTRAINT queues_fk;
       public          postgres    false    219    215    3206            3      x������ � �      -      x������ � �      1      x������ � �      /      x������ � �      +   N   x��H��H������4202�54�52V02�25�25�336��0�60�4��!?���0� O7OW0���ϕ+F��� �D�     