CREATE TABLE `users` (
                         `id` BIGINT NOT NULL AUTO_INCREMENT UNIQUE,
                         `email` VARCHAR(100) NOT NULL UNIQUE,
                         `password_hash` VARCHAR(255) NOT NULL,
                         `first_name` VARCHAR(50) NOT NULL,
                         `last_name` VARCHAR(50) NOT NULL,
                         `middle_name` VARCHAR(50),
                         `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         PRIMARY KEY (`id`)
);

CREATE TABLE `accounts` (
                            `id` BIGINT NOT NULL AUTO_INCREMENT UNIQUE,
                            `user_id` BIGINT NOT NULL,
                            `name` VARCHAR(40) NOT NULL,
                            `account_number` VARCHAR(20) NOT NULL UNIQUE,
                            `status` ENUM('ACTIVE', 'CLOSED') NOT NULL DEFAULT 'ACTIVE',
                            `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            PRIMARY KEY (`id`)
);

CREATE TABLE `transactions` (
                                `id` BIGINT NOT NULL AUTO_INCREMENT UNIQUE,
                                `source_account_id` BIGINT,
                                `recipient_account_id` BIGINT NOT NULL,
                                `amount` BIGINT NOT NULL CHECK (`amount` > 0),
                                `type` ENUM('TRANSFER', 'INITIAL_DEPOSIT') NOT NULL DEFAULT 'TRANSFER',
                                `status` ENUM('PENDING', 'COMPLETED', 'FAILED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
                                `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                PRIMARY KEY (`id`)
);

ALTER TABLE `accounts`
    ADD CONSTRAINT fk_accounts_user_id
        FOREIGN KEY (`user_id`) REFERENCES `users`(`id`)
            ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE `transactions`
    ADD CONSTRAINT fk_transactions_source_account_id
        FOREIGN KEY (`source_account_id`) REFERENCES `accounts`(`id`)
            ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE `transactions`
    ADD CONSTRAINT fk_transactions_recipient_account_id
        FOREIGN KEY (`recipient_account_id`) REFERENCES `accounts`(`id`)
            ON UPDATE CASCADE ON DELETE CASCADE;