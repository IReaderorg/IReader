package ir.kazemcodes.infinity.base_feature.util

import ir.kazemcodes.infinity.explore_feature.data.model.Book

object BookTest {



    val bookTest = Book.create().apply {
        bookName = "The First Order"
        coverLink = "https://readwebnovels.net/wp-content/uploads/2020/05/The-First-Order451-193x278.jpg"
        link = "https://readwebnovels.net/novel/the-first-order/"
        author = "The Speaking Pork Trotter,会说话的肘子"
        translator = "Webnovel"
        description = """This is a brand new story. Survive the darkness, see the light There is no right or wrong, it just depends on which side you are standing on. To be a god, or to be a man. To be good, or to be evil. Just what is…the highest order of weapon that humanity has? —————— After a great catastrophe struck, the world was set back many years and humanity started living in anarchy. With time, society started building up again and people were now living in walled strongholds and fringe towns across the land. Humans have also become distrustful and ruthless in an unforgiving society where the strong survive and the weak are eliminated. Growing up in such an era, Ren Xiaosu had to fend for himself. After an incident, he gained supernatural powers…"""
        category = "ACTION , Adventure"
        status = 1
        inLibrary = false
    }

    
}