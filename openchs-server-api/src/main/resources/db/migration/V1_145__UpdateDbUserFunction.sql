DROP FUNCTION IF EXISTS create_db_user(inrolname text, inpassword text);
create function create_db_user(inrolname text, inpassword text) returns bigint
    language plpgsql
as
$$
BEGIN
    IF NOT EXISTS(SELECT rolname FROM pg_roles WHERE rolname = inrolname)
    THEN
        EXECUTE 'CREATE USER ' || quote_ident(inrolname) || ' WITH PASSWORD ' || quote_literal(inpassword);
    END IF;
    EXECUTE 'GRANT ' || quote_ident(inrolname) || ' TO organisation_user';
    PERFORM grant_all_on_all(inrolname);
    PERFORM revoke_permissions_on_account(inrolname);
    RETURN 1;
END
$$;