package com.github.senocak.domain

import com.github.senocak.util.RoleName
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.io.Serializable
import java.util.Date
import java.util.UUID
import org.hibernate.annotations.DynamicUpdate

@MappedSuperclass
open class BaseDomain(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    @Column var createdAt: Date = Date(),
    @Column var updatedAt: Date = Date()
) : Serializable

@Entity
@Table(name = "users", uniqueConstraints = [
    UniqueConstraint(columnNames = ["email"])
])
@DynamicUpdate // TODO https://twitter.com/NiestrojRobert/status/1711280585111716218
class User(
    @Column var name: String,
    @Column var email: String,
    @Column var password: String?,

    @JoinTable(name = "user_roles",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "role_id")]
    )
    @ManyToMany(fetch = FetchType.EAGER)
    var roles: List<Role> = arrayListOf(),

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    var schedulers: MutableList<Scheduler> = ArrayList(),
) : BaseDomain()

@Entity
@Table(name = "roles")
class Role(@Column @Enumerated(EnumType.STRING) var name: RoleName? = null) : BaseDomain()

@Entity
@Table(name = "scheduler", uniqueConstraints = [
    UniqueConstraint(columnNames = ["name", "group"])
])
class Scheduler(
    @Enumerated(EnumType.STRING) @Column var status: Status = Status.UNKNOWN,
    @Column(nullable = true) var statusReason: String? = null,
    @Column val name: String,
    @Column(nullable = true) val description: String? = null,
    @Column val groupName: String,
    @Column val className: String,
    @Column val cron: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "user_id",
        referencedColumnName = "id",
        foreignKey = ForeignKey(name = "fk_scheduler_user_id")
    )
    var user: User,

    @Column(columnDefinition = "TEXT", nullable = true)
    @ElementCollection
    var result: MutableList<String> = arrayListOf(),
): BaseDomain()

enum class Status {
    UNKNOWN,
    ENABLED,
    DISABLED
}
