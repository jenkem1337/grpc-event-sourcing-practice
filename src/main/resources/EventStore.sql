--
-- PostgreSQL database dump
--

-- Dumped from database version 16.3
-- Dumped by pg_dump version 16.3

-- Started on 2025-02-28 21:24:24

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 215 (class 1259 OID 287006)
-- Name: entities; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.entities (
    id uuid NOT NULL,
    type text NOT NULL,
    version bigint NOT NULL
);


ALTER TABLE public.entities OWNER TO postgres;

--
-- TOC entry 216 (class 1259 OID 287014)
-- Name: events; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.events (
    id uuid NOT NULL,
    aggregate_id uuid NOT NULL,
    type text NOT NULL,
    data jsonb NOT NULL,
    version bigint NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.events OWNER TO postgres;

--
-- TOC entry 217 (class 1259 OID 295197)
-- Name: snapshots; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.snapshots (
    id uuid NOT NULL,
    aggregate_id uuid NOT NULL,
    aggregate_type text NOT NULL,
    data jsonb NOT NULL,
    version bigint NOT NULL
);


ALTER TABLE public.snapshots OWNER TO postgres;

--
-- TOC entry 4643 (class 2606 OID 287012)
-- Name: entities entity_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.entities
    ADD CONSTRAINT entity_pkey PRIMARY KEY (id);


--
-- TOC entry 4646 (class 2606 OID 287021)
-- Name: events event_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.events
    ADD CONSTRAINT event_pkey PRIMARY KEY (id);


--
-- TOC entry 4650 (class 2606 OID 295203)
-- Name: snapshots snapshots_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.snapshots
    ADD CONSTRAINT snapshots_pkey PRIMARY KEY (id);


--
-- TOC entry 4647 (class 1259 OID 287027)
-- Name: idx_aggregate_id_version; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX idx_aggregate_id_version ON public.events USING btree (aggregate_id, version);


--
-- TOC entry 4644 (class 1259 OID 287013)
-- Name: idx_entity_type; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_entity_type ON public.entities USING btree (type);


--
-- TOC entry 4648 (class 1259 OID 295204)
-- Name: snapshots_aggregate_version_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX snapshots_aggregate_version_idx ON public.snapshots USING btree (aggregate_id, version);


--
-- TOC entry 4651 (class 2606 OID 287022)
-- Name: events fk_entity_id_event_aggregate_id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.events
    ADD CONSTRAINT fk_entity_id_event_aggregate_id FOREIGN KEY (aggregate_id) REFERENCES public.entities(id) MATCH FULL ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 4652 (class 2606 OID 295205)
-- Name: snapshots fk_snapshots_entities; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.snapshots
    ADD CONSTRAINT fk_snapshots_entities FOREIGN KEY (aggregate_id) REFERENCES public.entities(id) ON DELETE CASCADE;


-- Completed on 2025-02-28 21:24:24

--
-- PostgreSQL database dump complete
--

